package com.skkil.sync.post.dto.summary;

import com.skkil.sync.post.model.PostType;
import com.skkil.sync.project.dto.summary.ProjectSummary;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.OffsetDateTime;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record PostSummary(
    Long id,
    String slug,
    PostType type,
    UserSummary author,
    @Nullable ProjectSummary project,
    boolean resolved,
    OffsetDateTime createdAt) {}
