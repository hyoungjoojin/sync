package com.skkil.sync.media.service.domain;

import com.skkil.sync.media.enums.MediaStatus;
import com.skkil.sync.media.enums.MediaType;
import com.skkil.sync.media.exception.MediaNotFoundException;
import com.skkil.sync.media.exception.MediaNotUploadedException;
import com.skkil.sync.media.exception.MediaTooLargeException;
import com.skkil.sync.media.exception.UnsupportedMediaTypeException;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import io.awspring.cloud.s3.S3Template;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@Slf4j
public class MediaDomainService {

  private final MediaRepository mediaRepository;
  private final S3Template s3Template;
  private final S3Client s3Client;

  public MediaDomainService(
      MediaRepository mediaRepository, S3Template s3Template, S3Client s3Client) {
    this.mediaRepository = mediaRepository;
    this.s3Template = s3Template;
    this.s3Client = s3Client;
  }

  /**
   * Marks a pending media file as uploaded, after confirming that the object actually exists in S3
   * and that it matches what the uploader declared. Every caller that attaches media to a post,
   * project or profile must go through here — marking media uploaded on the client's word alone
   * leaves rows pointing at objects that were never stored.
   */
  @Transactional
  public Media linkMedia(Long requesterId, Long mediaId) {
    Media media = getUnlinkedMedia(requesterId, mediaId);
    HeadObjectResponse object = headObject(media);

    verifySize(media, object);
    verifyMediaType(media, object);

    media.markAsUploaded();

    return media;
  }

  private HeadObjectResponse headObject(Media media) {
    try {
      return s3Client.headObject(
          HeadObjectRequest.builder().bucket(media.getBucket()).key(media.getKey()).build());
    } catch (NoSuchKeyException exception) {
      log.debug("Media with ID {} has no object at {}.", media.getId(), media.getKey());

      throw new MediaNotUploadedException(media.getId(), exception);
    }
  }

  private void verifySize(Media media, HeadObjectResponse object) {
    long maxFileSize = media.getMediaType().maxFileSizeBytes();
    Long contentLength = object.contentLength();

    if (contentLength != null && contentLength > maxFileSize) {
      log.debug(
          "Media with ID {} was declared as {} bytes but the stored object is {} bytes.",
          media.getId(),
          media.getFileSize(),
          contentLength);

      throw new MediaTooLargeException(contentLength, maxFileSize);
    }
  }

  /**
   * The declared media type decides how the object is served back, so the stored object must carry
   * exactly that content type. {@link com.skkil.sync.media.service.MediaService} signs it into the
   * presigned PUT, which makes any other value unuploadable — this re-checks it because signature
   * enforcement lives in the object store, and a store that does not enforce signed headers (or a
   * URL issued before the content type was pinned) would otherwise leave the type unverified.
   */
  private void verifyMediaType(Media media, HeadObjectResponse object) {
    String contentType = object.contentType();

    boolean matchesDeclaredType =
        MediaType.from(contentType)
            .filter(mediaType -> mediaType == media.getMediaType())
            .isPresent();

    if (!matchesDeclaredType) {
      log.debug(
          "Media with ID {} was declared as {} but the stored object is {}.",
          media.getId(),
          media.getMediaType().getMimeType(),
          contentType);

      throw new UnsupportedMediaTypeException(contentType);
    }
  }

  private Media getUnlinkedMedia(Long requesterId, Long mediaId) {
    Media media =
        mediaRepository.findById(mediaId).orElseThrow(() -> new MediaNotFoundException(mediaId));

    if (!requesterId.equals(media.getUploader().getId())) {
      log.debug(
          "Requester with ID {} is not the uploader of media with ID {}. Uploader ID: {}",
          requesterId,
          media.getId(),
          media.getUploader().getId());

      throw new MediaNotFoundException(mediaId);
    }

    if (media.getStatus() != MediaStatus.PENDING) {
      log.debug(
          "Media with ID {} is not in a valid state. Current status: {}",
          media.getId(),
          media.getStatus());

      throw new MediaNotFoundException(media.getId());
    }

    return media;
  }

  @Transactional(readOnly = true)
  public URL generatePresignedGetUrl(Media media) {
    if (media.getStatus() != MediaStatus.UPLOADED) {
      log.debug(
          "Media with ID {} is not in a valid state. Current status: {}",
          media.getId(),
          media.getStatus());

      throw new MediaNotFoundException(media.getId());
    }

    return s3Template.createSignedGetURL(media.getBucket(), media.getKey(), Duration.ofMinutes(10));
  }

  @Transactional(readOnly = true)
  public Map<Long, URL> generatePresignedGetUrlsByIds(List<Long> mediaIds) {
    List<Media> medias = mediaRepository.findAllByIdIn(mediaIds);

    Map<Long, URL> mediaIdToUrl = new HashMap<>();
    for (Media media : medias) {
      mediaIdToUrl.put(media.getId(), generatePresignedGetUrl(media));
    }

    return mediaIdToUrl;
  }

  @Transactional(readOnly = true)
  public <T> Map<Long, URL> generatePresignedGetUrls(List<Media> medias) {
    Map<Long, URL> result = new HashMap<>();
    for (Media media : medias) {
      if (media == null) {
        continue;
      }

      result.put(media.getId(), generatePresignedGetUrl(media));
    }

    return result;
  }

  @Transactional(readOnly = true)
  public <T> Map<Long, URL> generatePresignedGetUrls(
      List<T> items, Function<T, Media> mediaExtractor) {
    Map<Long, URL> result = new HashMap<>();
    for (T item : items) {
      if (item == null) {
        continue;
      }

      Media media = mediaExtractor.apply(item);
      if (media == null) {
        continue;
      }

      result.put(media.getId(), generatePresignedGetUrl(media));
    }

    return result;
  }
}
