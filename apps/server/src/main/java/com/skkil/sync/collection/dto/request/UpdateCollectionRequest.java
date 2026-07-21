package com.skkil.sync.collection.dto.request;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateCollectionRequest(
    @Size(max = 255) @Nullable String name,
    @Nullable String description,
    @Nullable Boolean isPublic) {}
