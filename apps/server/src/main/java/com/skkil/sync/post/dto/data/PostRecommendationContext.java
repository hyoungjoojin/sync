package com.skkil.sync.post.dto.data;

import com.skkil.sync.post.model.PostScope;
import org.jspecify.annotations.Nullable;

public record PostRecommendationContext(Long requesterId, @Nullable PostScope scope) {}
