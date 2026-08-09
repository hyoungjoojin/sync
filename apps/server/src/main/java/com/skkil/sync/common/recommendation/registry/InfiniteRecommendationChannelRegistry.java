package com.skkil.sync.common.recommendation.registry;

import com.skkil.sync.common.recommendation.channel.InfiniteRecommendationChannel;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.common.util.pagination.model.Cursor;
import com.skkil.sync.common.util.pagination.service.PaginationService;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class InfiniteRecommendationChannelRegistry<TType extends Enum<TType>, TContext, TCandidate>
    extends RecommendationChannelRegistry<
        TType, InfiniteRecommendationChannel<TType, TContext, TCandidate, ? extends Cursor>> {

  private final PaginationService paginationService;

  public InfiniteRecommendationChannelRegistry(
      Class<TType> typeClass,
      TType defaultType,
      List<? extends InfiniteRecommendationChannel<TType, TContext, TCandidate, ? extends Cursor>>
          channels,
      PaginationService paginationService) {
    super(typeClass, defaultType, channels);
    this.paginationService = paginationService;
  }

  public CursorPaginationResponse<TCandidate> fetch(
      TContext context, CursorPaginationRequest request) {
    return fetch(context, defaultType, request);
  }

  public CursorPaginationResponse<TCandidate> fetch(
      TContext context, @Nullable TType type, CursorPaginationRequest request) {
    return paginate(resolve(type), context, request);
  }

  private <C extends Cursor> CursorPaginationResponse<TCandidate> paginate(
      InfiniteRecommendationChannel<TType, TContext, TCandidate, C> channel,
      TContext context,
      CursorPaginationRequest request) {
    return paginationService.paginate(
        channel.getCandidateFetcher(context), channel.getPaginationProvider(), request);
  }
}
