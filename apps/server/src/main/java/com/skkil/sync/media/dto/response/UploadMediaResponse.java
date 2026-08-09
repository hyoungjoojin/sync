package com.skkil.sync.media.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

/**
 * A presigned PUT URL for a newly registered, not yet uploaded media row.
 *
 * @param contentType the exact {@code Content-Type} header the PUT to {@code uploadUrl} must carry.
 *     It is part of the signature, so any other value is rejected by the object store.
 */
@Builder
public record UploadMediaResponse(
    String mediaId, String uploadUrl, String contentType, LocalDateTime expiresAt) {}
