package com.skkil.sync.promotion.dto.request;

import com.skkil.sync.promotion.model.PromotionFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PromotionFieldRequest(
    @NotBlank String key,
    @NotNull PromotionFieldType type,
    @NotBlank String label,
    boolean required) {}
