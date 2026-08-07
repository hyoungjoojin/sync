package com.skkil.sync.promotion.dto.response;

import com.skkil.sync.promotion.model.PromotionFieldType;

public record PromotionFieldResponse(
    String key, PromotionFieldType type, String label, boolean required) {}
