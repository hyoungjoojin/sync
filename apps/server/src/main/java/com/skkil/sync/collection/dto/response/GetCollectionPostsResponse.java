package com.skkil.sync.collection.dto.response;

import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.post.dto.summary.PostSummary;
import org.jspecify.annotations.Nullable;

public record GetCollectionPostsResponse(CursorPaginationResponse<Item> posts) {

  public record Item(Long collectionPostId, @Nullable PostSummary post) {}
}
