package com.skkil.sync.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CreateCollectionRequest(
    @NotBlank @Size(max = 255) String name,
    @Nullable String description,
    @Nullable Boolean isPublic) {}
