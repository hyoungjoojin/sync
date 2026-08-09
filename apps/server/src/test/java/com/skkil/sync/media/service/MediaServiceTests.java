package com.skkil.sync.media.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.media.dto.request.UploadMediaRequest;
import com.skkil.sync.media.dto.response.UploadMediaResponse;
import com.skkil.sync.media.exception.UnsupportedMediaTypeException;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.UserService;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaServiceTests {

  private static final Long UPLOADER_ID = 1L;
  private static final Long MEDIA_ID = 10L;
  private static final Instant EXPIRATION = Instant.parse("2026-01-01T00:10:00Z");
  private static final String UPLOAD_URL = "https://s3.example.com/skkil-sync-media/object-key";

  @Mock private UserService userService;
  @Mock private MediaRepository mediaRepository;
  @Mock private S3Presigner s3Presigner;

  @Captor private ArgumentCaptor<PutObjectPresignRequest> presignRequestCaptor;

  private MediaService mediaService;

  @BeforeEach
  void setUp() {
    mediaService = new MediaService(userService, mediaRepository, s3Presigner);

    when(userService.getUserReference(UPLOADER_ID)).thenReturn(new User(UPLOADER_ID));
    when(mediaRepository.save(any(Media.class)))
        .thenAnswer(
            invocation -> {
              Media media = invocation.getArgument(0);
              media.setId(MEDIA_ID);

              return media;
            });
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(presignedPutObjectRequest());
  }

  @Test
  @DisplayName("[uploadMedia] 신고된 Content-Type을 presigned PUT 서명에 포함한다")
  void uploadMedia_signsDeclaredContentType() {
    mediaService.uploadMedia(UPLOADER_ID, request("image/png"));

    verify(s3Presigner).presignPutObject(presignRequestCaptor.capture());
    assertThat(presignRequestCaptor.getValue().putObjectRequest().contentType())
        .isEqualTo("image/png");
  }

  @Test
  @DisplayName("[uploadMedia] 클라이언트가 보낸 Content-Type이 아니라 정규화된 MIME 타입을 서명한다")
  void uploadMedia_signsNormalizedMimeType() {
    UploadMediaResponse response = mediaService.uploadMedia(UPLOADER_ID, request("IMAGE/PNG"));

    verify(s3Presigner).presignPutObject(presignRequestCaptor.capture());
    assertThat(presignRequestCaptor.getValue().putObjectRequest().contentType())
        .isEqualTo("image/png");
    assertThat(response.contentType()).isEqualTo("image/png");
  }

  @Test
  @DisplayName("[uploadMedia] 업로드가 보내야 하는 Content-Type을 응답으로 알려준다")
  void uploadMedia_returnsContentTypeToSend() {
    UploadMediaResponse response =
        mediaService.uploadMedia(UPLOADER_ID, request("application/pdf"));

    assertThat(response.mediaId()).isEqualTo(String.valueOf(MEDIA_ID));
    assertThat(response.uploadUrl()).isEqualTo(UPLOAD_URL);
    assertThat(response.contentType()).isEqualTo("application/pdf");
  }

  @Test
  @DisplayName("[uploadMedia] 만료 시각은 서명된 URL의 만료 시각을 그대로 쓴다")
  void uploadMedia_expiresAtMatchesSignatureExpiration() {
    UploadMediaResponse response = mediaService.uploadMedia(UPLOADER_ID, request("image/png"));

    assertThat(response.expiresAt())
        .isEqualTo(LocalDateTime.ofInstant(EXPIRATION, ZoneId.systemDefault()));
  }

  @Test
  @DisplayName("[uploadMedia] 허용되지 않는 타입이면 URL을 발급하지 않는다")
  void uploadMedia_unsupportedMediaType_doesNotPresign() {
    assertThatThrownBy(() -> mediaService.uploadMedia(UPLOADER_ID, request("application/zip")))
        .isInstanceOf(UnsupportedMediaTypeException.class);

    verify(s3Presigner, never()).presignPutObject(any(PutObjectPresignRequest.class));
  }

  @Test
  @DisplayName("[uploadMedia] 서명된 헤더 목록에 content-type이 들어간다")
  void uploadMedia_contentTypeIsPartOfTheSignature() {
    try (S3Presigner presigner =
        S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("access-key", "secret-key")))
            .build()) {
      mediaService = new MediaService(userService, mediaRepository, presigner);

      UploadMediaResponse response = mediaService.uploadMedia(UPLOADER_ID, request("image/png"));

      assertThat(response.uploadUrl()).contains("X-Amz-SignedHeaders=content-type%3Bhost");
    }
  }

  private UploadMediaRequest request(String mediaType) {
    return UploadMediaRequest.builder()
        .mediaType(mediaType)
        .fileName("test-file")
        .fileSize(1024L)
        .build();
  }

  private PresignedPutObjectRequest presignedPutObjectRequest() {
    return PresignedPutObjectRequest.builder()
        .expiration(EXPIRATION)
        .isBrowserExecutable(false)
        .signedHeaders(Map.of("content-type", List.of("image/png")))
        .httpRequest(
            SdkHttpRequest.builder().method(SdkHttpMethod.PUT).uri(URI.create(UPLOAD_URL)).build())
        .build();
  }
}
