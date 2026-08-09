package com.skkil.sync.post.dto.request;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdatePostSeriesRequest(@Size(max = 255) @Nullable String name) {}
