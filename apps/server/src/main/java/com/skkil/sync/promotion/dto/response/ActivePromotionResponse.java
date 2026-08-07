package com.skkil.sync.promotion.dto.response;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ActivePromotionResponse(
    Long id,
    List<PromotionFieldResponse> fields,
    @Nullable String projectHandle,
    @Nullable String postSlug,
    @Nullable PromotionSignupResponse signup) {}
