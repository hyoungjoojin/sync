package com.skkil.sync.media.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MediaTypeTests {

  @ParameterizedTest
  @ValueSource(strings = {"image/png", "image/jpeg", "image/webp", "image/svg+xml"})
  @DisplayName("[해석] 허용된 이미지 타입은 이미지로 분류된다")
  void from_imageTypes_areImages(String mimeType) {
    assertThat(MediaType.from(mimeType)).get().extracting(MediaType::isImage).isEqualTo(true);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "application/pdf",
        "text/plain",
        "text/csv",
        "text/markdown",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.oasis.opendocument.text"
      })
  @DisplayName("[해석] 허용된 문서 타입은 이미지가 아니다")
  void from_documentTypes_areNotImages(String mimeType) {
    assertThat(MediaType.from(mimeType)).get().extracting(MediaType::isImage).isEqualTo(false);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "text/html",
        "application/zip",
        "application/json",
        "video/mp4",
        "application/x-sh",
        ""
      })
  @DisplayName("[해석] 허용 목록에 없는 타입은 거부된다")
  void from_unsupportedTypes_returnsEmpty(String mimeType) {
    assertThat(MediaType.from(mimeType)).isEmpty();
  }

  @Test
  @DisplayName("[해석] 타입이 없으면 거부된다")
  void from_null_returnsEmpty() {
    assertThat(MediaType.from(null)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"TEXT/CSV", " text/csv ", "text/csv; charset=utf-8"})
  @DisplayName("[해석] 대소문자와 파라미터, 공백은 정규화 후 비교한다")
  void from_normalizesBeforeMatching(String mimeType) {
    assertThat(MediaType.from(mimeType)).contains(MediaType.TEXT_CSV);
  }

  @ParameterizedTest
  @EnumSource(MediaType.class)
  @DisplayName("[해석] 모든 타입은 자기 MIME 타입으로 되찾을 수 있다")
  void from_everyMimeTypeRoundTrips(MediaType mediaType) {
    assertThat(MediaType.from(mediaType.getMimeType())).contains(mediaType);
  }

  @Test
  @DisplayName("[크기] 이미지는 5MB, 그 외는 20MB까지 허용한다")
  void maxFileSizeBytes_differsForImages() {
    assertThat(MediaType.IMAGE_PNG.maxFileSizeBytes()).isEqualTo(5L * 1024 * 1024);
    assertThat(MediaType.APPLICATION_PDF.maxFileSizeBytes()).isEqualTo(20L * 1024 * 1024);
  }

  @Test
  @DisplayName("[이미지 목록] 이미지 타입만, 빠짐없이 담는다")
  void imageTypes_containsExactlyTheImageTypes() {
    assertThat(MediaType.imageTypes()).isNotEmpty().allMatch(MediaType::isImage);
    assertThat(MediaType.imageTypes())
        .containsExactlyInAnyOrderElementsOf(
            Arrays.stream(MediaType.values()).filter(MediaType::isImage).toList());
  }
}
