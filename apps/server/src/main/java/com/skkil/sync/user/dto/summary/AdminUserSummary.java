package com.skkil.sync.user.dto.summary;

import com.skkil.sync.user.constant.Role;
import java.time.Instant;
import lombok.Builder;

@Builder
public record AdminUserSummary(
    Long id,
    String handle,
    String name,
    String email,
    String profileImageUrl,
    Role role,
    boolean isOnboarded,
    boolean isEmailVerified,
    long followerCount,
    long followingCount,
    Instant createdAt,
    Instant deletedAt) {}
