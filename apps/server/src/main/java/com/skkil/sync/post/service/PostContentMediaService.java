package com.skkil.sync.post.service;

import com.skkil.sync.media.dto.MediaDto;
import com.skkil.sync.media.enums.MediaType;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.post.constants.PostPreviewProperties;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostMediaFile;
import com.skkil.sync.post.repository.PostMediaFileRepository;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PostContentMediaService {

  private final MediaDomainService mediaService;
  private final PostMediaFileRepository postMediaFileRepository;

  public PostContentMediaService(
      MediaDomainService mediaService, PostMediaFileRepository postMediaFileRepository) {
    this.mediaService = mediaService;
    this.postMediaFileRepository = postMediaFileRepository;
  }

  public List<MediaDto> getMediaFilesForPost(Long postId) {
    List<Media> medias =
        postMediaFileRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
            .map(PostMediaFile::getMedia)
            .toList();

    Map<Long, URL> urls = mediaService.generatePresignedGetUrls(medias);

    return medias.stream().map(media -> toMediaDto(media, urls)).toList();
  }

  /**
   * Only image media can be shown as a preview thumbnail, so attachments are filtered out before
   * the per-post limit is applied — a post whose first attachment is a document must still show as
   * many thumbnails as it has images.
   */
  public Map<Long, List<MediaDto>> getPreviewMediaForPosts(List<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, List<Media>> previewMediaByPostId = new LinkedHashMap<>();
    for (PostMediaFile postMediaFile :
        postMediaFileRepository.findByPostIdInAndMediaTypeIn(postIds, MediaType.imageTypes())) {
      List<Media> previewMedia =
          previewMediaByPostId.computeIfAbsent(
              postMediaFile.getPost().getId(), postId -> new ArrayList<>());

      if (previewMedia.size() < PostPreviewProperties.PREVIEW_MEDIA_MAX_COUNT) {
        previewMedia.add(postMediaFile.getMedia());
      }
    }

    Map<Long, URL> urls =
        mediaService.generatePresignedGetUrls(
            previewMediaByPostId.values().stream().flatMap(List::stream).toList());

    return previewMediaByPostId.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(media -> toMediaDto(media, urls)).toList()));
  }

  private static MediaDto toMediaDto(Media media, Map<Long, URL> urls) {
    return MediaDto.builder()
        .id(media.getId())
        .url(urls.get(media.getId()).toExternalForm())
        .fileName(media.getFileName())
        .fileSize(media.getFileSize())
        .mediaType(media.getMediaType().getMimeType())
        .build();
  }

  public List<Media> resolveMediaFilesForCreate(Long authorId, List<Long> mediaIds) {
    if (mediaIds == null || mediaIds.isEmpty()) {
      return List.of();
    }

    List<Media> mediaFiles = new ArrayList<>();
    for (Long mediaId : mediaIds) {
      mediaFiles.add(mediaService.linkMedia(authorId, mediaId));
    }

    return mediaFiles;
  }

  public List<Media> resolveMediaFilesForUpdate(Long authorId, Long postId, List<Long> mediaIds) {
    if (mediaIds == null || mediaIds.isEmpty()) {
      return List.of();
    }

    Map<Long, Media> currentMedia =
        postMediaFileRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
            .map(PostMediaFile::getMedia)
            .collect(Collectors.toMap(Media::getId, media -> media));

    List<Media> mediaFiles = new ArrayList<>();
    for (Long mediaId : new LinkedHashSet<>(mediaIds)) {
      Media media = currentMedia.get(mediaId);
      if (media == null) {
        media = mediaService.linkMedia(authorId, mediaId);
      }
      mediaFiles.add(media);
    }

    return mediaFiles;
  }

  public void replaceMediaFiles(Post post, List<Media> mediaFiles) {
    postMediaFileRepository.deleteAllByPostId(post.getId());
    savePostMediaFiles(post, mediaFiles);
  }

  public void savePostMediaFiles(Post post, List<Media> mediaFiles) {
    for (int i = 0; i < mediaFiles.size(); i++) {
      postMediaFileRepository.save(new PostMediaFile(post, mediaFiles.get(i), i));
    }
  }
}
