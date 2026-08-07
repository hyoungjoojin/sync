package com.skkil.sync.promotion.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreatePromotionSignupRequest(@NotNull Map<String, String> attachment) {}
