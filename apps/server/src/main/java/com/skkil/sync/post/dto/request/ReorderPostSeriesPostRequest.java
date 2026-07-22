package com.skkil.sync.post.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReorderPostSeriesPostRequest(@NotNull @Positive Integer position) {}
