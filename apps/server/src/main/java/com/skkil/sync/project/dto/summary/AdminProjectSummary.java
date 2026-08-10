package com.skkil.sync.project.dto.summary;

import com.skkil.sync.project.model.JoinPolicy;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import lombok.Builder;

@Builder
public record AdminProjectSummary(
    Long id,
    String handle,
    String name,
    String description,
    String website,
    boolean isPublic,
    JoinPolicy joinPolicy,
    long followerCount,
    long teammateCount,
    UserSummary owner,
    Instant createdAt,
    String iconUrl) {}
