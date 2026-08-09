package com.skkil.sync.collection.dto.data;

import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record CollectionPostDto(
    Long collectionPostId, @Nullable Long postId, OffsetDateTime createdAt) {}
