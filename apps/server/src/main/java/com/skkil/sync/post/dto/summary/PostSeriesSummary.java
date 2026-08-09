package com.skkil.sync.post.dto.summary;

import com.skkil.sync.post.model.PostScope;
import org.jspecify.annotations.Nullable;

public record PostSeriesSummary(
    String externalId,
    String name,
    PostScope scope,
    long postCount,
    Long creatorId,
    @Nullable String projectHandle) {}
