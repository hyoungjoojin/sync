package com.skkil.sync.collection.dto.summary;

import com.skkil.sync.collection.model.CollectionScope;
import org.jspecify.annotations.Nullable;

public record CollectionSummary(
    String externalId,
    String name,
    @Nullable String description,
    CollectionScope scope,
    boolean isPublic,
    long postCount,
    Long creatorId,
    @Nullable String projectHandle) {}
