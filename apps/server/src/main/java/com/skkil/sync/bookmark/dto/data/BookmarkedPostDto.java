package com.skkil.sync.bookmark.dto.data;

import com.skkil.sync.post.model.PostType;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record BookmarkedPostDto(
    Long id,
    String slug,
    PostType type,
    Long bookmarkId,
    OffsetDateTime bookmarkedAt,
    Long authorId,
    @Nullable String projectHandle,
    @Nullable String projectName,
    @Nullable String projectDescription,
    @Nullable String projectWebsite,
    @Nullable Boolean projectIsPublic,
    String content,
    Long likeCount,
    Long commentCount,
    Boolean bookmarked,
    Boolean resolved,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
