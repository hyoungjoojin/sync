package com.skkil.sync.bookmark.dto.response;

import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.post.dto.summary.PostSummary;
import java.time.OffsetDateTime;
import lombok.Builder;

public record GetBookmarkedPostsResponse(CursorPaginationResponse<Post> posts) {

  @Builder
  public static record Post(
      PostSummary summary,
      String content,
      Long likeCount,
      Long commentCount,
      boolean bookmarked,
      OffsetDateTime bookmarkedAt) {}
}
