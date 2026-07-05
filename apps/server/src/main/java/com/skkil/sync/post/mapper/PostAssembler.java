package com.skkil.sync.post.mapper;

import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.media.dto.MediaDto;
import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.dto.response.GetPostResponse;
import com.skkil.sync.post.dto.response.GetPostsResponse;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.user.dto.summary.UserSummary;
import com.skkil.sync.user.mapper.UserAssembler;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostAssembler {

  private final PostMapper postMapper;

  private final UserAssembler userAssembler;

  public PostAssembler(PostMapper postMapper, UserAssembler userAssembler) {
    this.postMapper = postMapper;
    this.userAssembler = userAssembler;
  }

  public GetPostResponse toGetPostResponse(PostDto post, List<MediaDto> media) {
    UserSummary author = userAssembler.toUserSummary(post.authorId());

    return GetPostResponse.builder()
        .summary(toPostSummary(post, author))
        .content(postMapper.toContent(post, media))
        .likeCount(post.likeCount())
        .commentCount(post.commentCount())
        .bookmarked(Boolean.TRUE.equals(post.bookmarked()))
        .build();
  }

  public GetPostsResponse.Post toPostResponse(PostDto post, UserSummary author) {
    return GetPostsResponse.Post.builder()
        .summary(toPostSummary(post, author))
        .content(post.content())
        .build();
  }

  public CursorPaginationResponse<GetPostsResponse.Post> toPostResponses(
      CursorPaginationResponse<PostDto> posts) {
    return posts.mapWithLookup(
        PostDto::authorId,
        userAssembler::toUserSummaries,
        (post, authors) -> toPostResponse(post, authors.get(post.authorId())));
  }

  public List<GetPostsResponse.Post> toPostResponses(List<PostDto> posts) {
    var authorIds = posts.stream().map(PostDto::authorId).distinct().toList();
    var authors = userAssembler.toUserSummaries(authorIds);

    return posts.stream().map(post -> toPostResponse(post, authors.get(post.authorId()))).toList();
  }

  private PostSummary toPostSummary(PostDto post, UserSummary author) {
    var project = post.projectHandle() == null ? null : postMapper.toProjectSummary(post);
    return postMapper.toPostSummary(post, author, project);
  }
}
