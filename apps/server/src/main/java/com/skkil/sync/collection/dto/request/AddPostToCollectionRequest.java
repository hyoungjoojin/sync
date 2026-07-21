package com.skkil.sync.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record AddPostToCollectionRequest(
    @Nullable String projectHandle, @NotBlank String postHandle) {}
