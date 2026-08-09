package com.skkil.sync.promotion.dto.response;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record PromotionResponse(
    Long id,
    boolean active,
    List<PromotionFieldResponse> fields,
    @Nullable String projectHandle,
    @Nullable String postSlug,
    // Denormalized display label for the admin list — the linked post's own title.
    // Not stored on the promotion itself; fetched alongside it.
    @Nullable String postTitle,
    Instant createdAt) {}
