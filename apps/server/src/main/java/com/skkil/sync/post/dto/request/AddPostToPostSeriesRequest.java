package com.skkil.sync.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

public record AddPostToPostSeriesRequest(
    @Nullable String projectHandle,
    @NotBlank String postHandle,
    @Positive @Nullable Integer position) {}
