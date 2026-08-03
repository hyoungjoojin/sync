package com.skkil.sync.post.dto.data;

import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.project.model.JoinPolicy;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record PostDto(
    Long id,
    PostType type,
    PostStatus status,
    String slug,
    @Nullable String title,
    Long authorId,
    @Nullable String projectHandle,
    @Nullable String projectName,
    @Nullable String projectDescription,
    @Nullable String projectWebsite,
    @Nullable Boolean projectIsPublic,
    @Nullable JoinPolicy projectJoinPolicy,
    @Nullable Long projectFollowerCount,
    @Nullable String content,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Long likeCount,
    Long commentCount,
    Boolean liked,
    Boolean bookmarked,
    Boolean resolved,
    String preview,
    int mediaCount,
    int wordCount,
    @Nullable Long coverMediaId,
    Boolean isSeriesPost,
    @Nullable OffsetDateTime pinnedAt,
    @Nullable OffsetDateTime sortKey) {}
