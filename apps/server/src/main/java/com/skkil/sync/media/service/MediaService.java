package com.skkil.sync.media.service;

import com.skkil.sync.media.dto.request.UploadMediaRequest;
import com.skkil.sync.media.dto.response.UploadMediaResponse;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.UserService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class MediaService {

  private static final String BUCKET = "skkil-sync-media";
  private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(10);

  private final UserService userService;
  private final MediaRepository mediaRepository;
  private final S3Presigner s3Presigner;

  public MediaService(
      UserService userService, MediaRepository mediaRepository, S3Presigner s3Presigner) {
    this.userService = userService;
    this.mediaRepository = mediaRepository;
    this.s3Presigner = s3Presigner;
  }

  @Transactional
  public UploadMediaResponse uploadMedia(Long uploaderId, UploadMediaRequest request) {
    User uploader = userService.getUserReference(uploaderId);
    String key = UUID.randomUUID().toString();

    Media media =
        Media.builder()
            .uploader(uploader)
            .mediaType(request.mediaType())
            .bucket(BUCKET)
            .key(key)
            .fileName(request.fileName())
            .fileSize(request.fileSize())
            .build();
    mediaRepository.save(media);

    String contentType = media.getMediaType().getMimeType();
    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(key)
                        .contentType(contentType)
                        .build())
                .build());

    return UploadMediaResponse.builder()
        .mediaId(String.valueOf(media.getId()))
        .uploadUrl(presignedRequest.url().toExternalForm())
        .contentType(contentType)
        .expiresAt(LocalDateTime.ofInstant(presignedRequest.expiration(), ZoneId.systemDefault()))
        .build();
  }
}
