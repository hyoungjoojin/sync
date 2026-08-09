package com.skkil.sync.post.dto.response;

import com.skkil.sync.post.dto.summary.PostSeriesSummary;
import java.util.List;

public record GetPostSeriesListResponse(List<PostSeriesSummary> series) {}
