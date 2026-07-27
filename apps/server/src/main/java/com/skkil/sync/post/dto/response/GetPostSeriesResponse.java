package com.skkil.sync.post.dto.response;

import com.skkil.sync.post.dto.summary.PostSeriesSummary;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record GetPostSeriesResponse(
    @Nullable PostSeriesSummary series, @Nullable Long currentSeriesPostId, List<Post> posts) {

  public record Post(
      Long seriesPostId, int position, @Nullable String slug, @Nullable String title) {}
}
