package com.skkil.sync.post.service.recommendation;

import com.skkil.sync.common.util.pagination.interfaces.CursorPaginationDataFetcher;
import com.skkil.sync.common.util.pagination.keyset.KeysetCursorPaginationProvider;
import com.skkil.sync.post.dto.data.PostRecommendationCandidate;
import com.skkil.sync.post.dto.data.PostRecommendationContext;
import com.skkil.sync.post.dto.data.PostRecommendationCursor;
import com.skkil.sync.post.model.PostRecommendationType;
import com.skkil.sync.post.repository.PostRecommendationQueryRepository;
import com.skkil.sync.post.repository.pagination.PostRecommendationPaginationProvider;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class RecentPostRecommendationChannel implements PostRecommendationChannel {

  private final PostRecommendationQueryRepository postRecommendationQueryRepository;

  public RecentPostRecommendationChannel(
      PostRecommendationQueryRepository postRecommendationQueryRepository) {
    this.postRecommendationQueryRepository = postRecommendationQueryRepository;
  }

  @Override
  public PostRecommendationType getType() {
    return PostRecommendationType.RECENT;
  }

  @Override
  public CursorPaginationDataFetcher<PostRecommendationCandidate> getCandidateFetcher(
      PostRecommendationContext context) {
    return postRecommendationQueryRepository.getCandidates(DSL.noCondition(), context);
  }

  @Override
  public KeysetCursorPaginationProvider<PostRecommendationCandidate, PostRecommendationCursor>
      getPaginationProvider() {
    return PostRecommendationPaginationProvider.CREATED_AT;
  }
}
