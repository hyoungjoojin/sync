package com.skkil.sync.comment.dto.data;

import java.time.OffsetDateTime;

public record CommentDto(
    Long id,
    Long authorId,
    String content,
    Boolean deleted,
    Boolean accepted,
    Long likeCount,
    Boolean liked,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
