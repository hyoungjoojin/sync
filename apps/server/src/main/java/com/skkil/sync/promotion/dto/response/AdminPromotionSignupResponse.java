package com.skkil.sync.promotion.dto.response;

import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import java.util.Map;

public record AdminPromotionSignupResponse(
    Long id, UserSummary user, Map<String, String> attachment, Instant createdAt) {}
