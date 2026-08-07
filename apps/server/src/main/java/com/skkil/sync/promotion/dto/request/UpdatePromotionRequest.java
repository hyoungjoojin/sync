package com.skkil.sync.promotion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdatePromotionRequest(
    @NotNull List<@Valid PromotionFieldRequest> fields,
    @NotBlank String projectHandle,
    @NotBlank String postSlug) {}
