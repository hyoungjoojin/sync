package com.skkil.sync.post.dto.data;

import com.skkil.sync.post.model.PostType;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record PostDto(
    Long id,
    PostType type,
    String slug,
    Long authorId,
    @Nullable String title,
    @Nullable String projectHandle,
    @Nullable String projectName,
    @Nullable String projectDescription,
    @Nullable String projectWebsite,
    @Nullable Boolean projectIsPublic,
    String content,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Long likeCount,
    Long commentCount,
    Boolean bookmarked,
    Boolean resolved,
    @Nullable OffsetDateTime bookmarkedAt) {}
