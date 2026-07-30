package com.skkil.sync.media.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The closed set of media types the platform accepts. Membership <em>is</em> the allowlist: a MIME
 * type with no constant here cannot be stored, so there is no separate kind column to keep in sync
 * and no free-form content type to validate after the fact.
 */
public enum MediaType {
  IMAGE_APNG("image/apng"),
  IMAGE_AVIF("image/avif"),
  IMAGE_BMP("image/bmp"),
  IMAGE_GIF("image/gif"),
  IMAGE_HEIC("image/heic"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_PNG("image/png"),
  IMAGE_SVG_XML("image/svg+xml"),
  IMAGE_TIFF("image/tiff"),
  IMAGE_WEBP("image/webp"),
  APPLICATION_PDF("application/pdf"),
  TEXT_PLAIN("text/plain"),
  TEXT_CSV("text/csv"),
  TEXT_MARKDOWN("text/markdown"),
  APPLICATION_MSWORD("application/msword"),
  APPLICATION_VND_MS_EXCEL("application/vnd.ms-excel"),
  APPLICATION_VND_MS_POWERPOINT("application/vnd.ms-powerpoint"),
  APPLICATION_VND_OPENXMLFORMATS_WORDPROCESSINGML_DOCUMENT(
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  APPLICATION_VND_OPENXMLFORMATS_SPREADSHEETML_SHEET(
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
  APPLICATION_VND_OPENXMLFORMATS_PRESENTATIONML_PRESENTATION(
      "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
  APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text"),
  APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet"),
  APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION(
      "application/vnd.oasis.opendocument.presentation");

  private static final String IMAGE_PREFIX = "image/";

  private static final long IMAGE_MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
  private static final long FILE_MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

  private static final Map<String, MediaType> BY_MIME_TYPE =
      Arrays.stream(values())
          .collect(Collectors.toUnmodifiableMap(MediaType::getMimeType, Function.identity()));

  private static final List<MediaType> IMAGE_TYPES =
      Arrays.stream(values()).filter(MediaType::isImage).toList();

  private final String mimeType;

  MediaType(String mimeType) {
    this.mimeType = mimeType;
  }

  /** Resolves a client-declared MIME type. An empty result means the type is not accepted. */
  public static Optional<MediaType> from(String mimeType) {
    if (mimeType == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(BY_MIME_TYPE.get(normalize(mimeType)));
  }

  /** Every type that renders as an image, for queries that must exclude attachments. */
  public static List<MediaType> imageTypes() {
    return IMAGE_TYPES;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean isImage() {
    return mimeType.startsWith(IMAGE_PREFIX);
  }

  public long maxFileSizeBytes() {
    return isImage() ? IMAGE_MAX_FILE_SIZE_BYTES : FILE_MAX_FILE_SIZE_BYTES;
  }

  private static String normalize(String mimeType) {
    int parameterIndex = mimeType.indexOf(';');
    String withoutParameters =
        parameterIndex < 0 ? mimeType : mimeType.substring(0, parameterIndex);

    return withoutParameters.trim().toLowerCase(Locale.ROOT);
  }
}
