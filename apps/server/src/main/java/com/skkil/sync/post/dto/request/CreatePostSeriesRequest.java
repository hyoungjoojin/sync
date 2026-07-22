package com.skkil.sync.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostSeriesRequest(@NotBlank @Size(max = 255) String name) {}
