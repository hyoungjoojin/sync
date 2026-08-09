package com.skkil.sync.promotion.dto.response;

import java.time.Instant;
import java.util.Map;

public record PromotionSignupResponse(Map<String, String> attachment, Instant createdAt) {}
