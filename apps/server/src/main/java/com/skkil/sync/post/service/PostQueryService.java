package com.skkil.sync.post.service;

import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.service.PaginationService;
import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.dto.response.GetPostResponse;
import com.skkil.sync.post.dto.response.GetPostsResponse;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.mapper.PostAssembler;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.repository.PostQueryRepository;
import com.skkil.sync.post.repository.pagination.PostCursorPaginationProvider;
import com.skkil.sync.user.mapper.UserAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PostQueryService {

  private final PostQueryRepository postQueryRepository;
  private final PostContentMediaService contentMediaService;
  private final PostAssembler postAssembler;
  private final UserAssembler userAssembler;

  private final PaginationService paginationService;
  private final PostCursorPaginationProvider paginationProvider;

  public PostQueryService(
      PostQueryRepository postQueryRepository,
      PostContentMediaService contentMediaService,
      PostAssembler postAssembler,
      UserAssembler userAssembler,
      PostCursorPaginationProvider paginationProvider,
      PaginationService paginationService) {
    this.postQueryRepository = postQueryRepository;
    this.contentMediaService = contentMediaService;
    this.postAssembler = postAssembler;
    this.userAssembler = userAssembler;
    this.paginationProvider = paginationProvider;
    this.paginationService = paginationService;
  }

  @Transactional(readOnly = true)
  public GetPostsResponse getPosts(CursorPaginationRequest pagination) {
    var posts =
        paginationService
            .paginate(postQueryRepository.getPosts(), paginationProvider, pagination)
            .mapWithLookup(
                PostDto::authorId,
                userAssembler::toUserSummaries,
                (post, authors) ->
                    postAssembler.toPostResponse(post, authors.get(post.authorId())));

    return new GetPostsResponse(posts);
  }

  @Transactional(readOnly = true)
  public GetPostResponse getPostBySlug(Long requesterId, String slug) {
    var post =
        postQueryRepository
            .getPostBySlug(requesterId, slug)
            .orElseThrow(() -> new PostNotFoundException(slug));

    var media = contentMediaService.getMediaFilesForPost(post.id());

    return postAssembler.toGetPostResponse(post, media);
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasPermission(#userId, 'PROFILE', 'READ')")
  public GetPostsResponse getUserPosts(Long userId, CursorPaginationRequest pagination) {
    var posts =
        paginationService
            .paginate(postQueryRepository.getPostsByUser(userId), paginationProvider, pagination)
            .mapWithLookup(
                PostDto::authorId,
                userAssembler::toUserSummaries,
                (post, authors) ->
                    postAssembler.toPostResponse(post, authors.get(post.authorId())));

    return new GetPostsResponse(posts);
  }

  @Transactional(readOnly = true)
  public GetPostsResponse getPostsByProject(
      String handle, PostType type, CursorPaginationRequest pagination) {
    var posts =
        paginationService
            .paginate(
                postQueryRepository.getPostsByProject(handle, type), paginationProvider, pagination)
            .mapWithLookup(
                PostDto::authorId,
                userAssembler::toUserSummaries,
                (post, authors) ->
                    postAssembler.toPostResponse(post, authors.get(post.authorId())));

    return new GetPostsResponse(posts);
  }
}
