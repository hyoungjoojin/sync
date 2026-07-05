package com.skkil.sync.bookmark.mapper;

import com.skkil.sync.bookmark.dto.data.BookmarkedPostDto;
import com.skkil.sync.bookmark.dto.response.GetBookmarkedPostsResponse;
import com.skkil.sync.user.dto.summary.UserSummary;
import org.springframework.stereotype.Component;

@Component
public class PostBookmarkAssembler {

  private final PostBookmarkMapper postBookmarkMapper;

  public PostBookmarkAssembler(PostBookmarkMapper postBookmarkMapper) {
    this.postBookmarkMapper = postBookmarkMapper;
  }

  public GetBookmarkedPostsResponse.Post toBookmarkedPostResponse(
      BookmarkedPostDto post, UserSummary author) {
    var project = post.projectHandle() == null ? null : postBookmarkMapper.toProjectSummary(post);
    var summary = postBookmarkMapper.toPostSummary(post, author, project);

    return GetBookmarkedPostsResponse.Post.builder()
        .summary(summary)
        .content(post.content())
        .likeCount(post.likeCount())
        .commentCount(post.commentCount())
        .bookmarked(Boolean.TRUE.equals(post.bookmarked()))
        .bookmarkedAt(post.bookmarkedAt())
        .build();
  }
}
