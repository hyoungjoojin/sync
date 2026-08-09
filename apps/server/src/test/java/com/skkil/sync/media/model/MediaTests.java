package com.skkil.sync.media.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skkil.sync.media.enums.MediaStatus;
import com.skkil.sync.media.enums.MediaType;
import com.skkil.sync.media.exception.MediaTooLargeException;
import com.skkil.sync.media.exception.UnsupportedMediaTypeException;
import com.skkil.sync.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MediaTests {

  @Test
  @DisplayName("[생성] MIME 타입을 허용된 미디어 타입으로 바꾸고 PENDING 상태로 시작한다")
  void create_resolvesMediaTypeFromMimeType() {
    Media image = createMedia("image/png", 1024L);
    Media file = createMedia("application/pdf", 1024L);

    assertThat(image.getMediaType()).isEqualTo(MediaType.IMAGE_PNG);
    assertThat(file.getMediaType()).isEqualTo(MediaType.APPLICATION_PDF);
    assertThat(image.getStatus()).isEqualTo(MediaStatus.PENDING);
  }

  @Test
  @DisplayName("[생성] 허용 목록에 없는 미디어 타입은 거부한다")
  void create_unsupportedMediaType_throws() {
    assertThatThrownBy(() -> createMedia("application/zip", 1024L))
        .isInstanceOf(UnsupportedMediaTypeException.class);
  }

  @Test
  @DisplayName("[생성] 타입별 최대 크기를 넘으면 거부한다")
  void create_oversizedFile_throws() {
    assertThatThrownBy(() -> createMedia("image/png", MediaType.IMAGE_PNG.maxFileSizeBytes() + 1))
        .isInstanceOf(MediaTooLargeException.class);

    assertThatThrownBy(
            () -> createMedia("application/pdf", MediaType.APPLICATION_PDF.maxFileSizeBytes() + 1))
        .isInstanceOf(MediaTooLargeException.class);
  }

  @Test
  @DisplayName("[생성] 이미지 제한을 넘는 크기라도 문서라면 허용한다")
  void create_sizeCapIsPerMediaType() {
    assertThatCode(() -> createMedia("application/pdf", MediaType.IMAGE_PNG.maxFileSizeBytes() + 1))
        .doesNotThrowAnyException();
  }

  private Media createMedia(String mediaType, Long fileSize) {
    return Media.builder()
        .uploader(new User(1L))
        .mediaType(mediaType)
        .bucket("test-bucket")
        .key("test-key")
        .fileName("test-file")
        .fileSize(fileSize)
        .build();
  }
}
