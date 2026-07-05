package com.skkil.sync.bookmark.service;

import com.skkil.sync.bookmark.dto.data.BookmarkedPostDto;
import com.skkil.sync.bookmark.dto.response.GetBookmarkedPostsResponse;
import com.skkil.sync.bookmark.mapper.PostBookmarkAssembler;
import com.skkil.sync.bookmark.repository.PostBookmarkQueryRepository;
import com.skkil.sync.bookmark.repository.pagination.BookmarkedPostCursorPaginationProvider;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.service.PaginationService;
import com.skkil.sync.user.mapper.UserAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostBookmarkQueryService {

  private final PostBookmarkQueryRepository postBookmarkQueryRepository;
  private final PostBookmarkAssembler postBookmarkAssembler;
  private final UserAssembler userAssembler;
  private final BookmarkedPostCursorPaginationProvider paginationProvider;
  private final PaginationService paginationService;

  public PostBookmarkQueryService(
      PostBookmarkQueryRepository postBookmarkQueryRepository,
      PostBookmarkAssembler postBookmarkAssembler,
      UserAssembler userAssembler,
      BookmarkedPostCursorPaginationProvider paginationProvider,
      PaginationService paginationService) {
    this.postBookmarkQueryRepository = postBookmarkQueryRepository;
    this.postBookmarkAssembler = postBookmarkAssembler;
    this.userAssembler = userAssembler;
    this.paginationProvider = paginationProvider;
    this.paginationService = paginationService;
  }

  @Transactional(readOnly = true)
  public GetBookmarkedPostsResponse getBookmarkedPosts(
      Long userId, CursorPaginationRequest pagination) {
    var bookmarkedPosts =
        paginationService
            .paginate(
                postBookmarkQueryRepository.getBookmarkedPosts(userId),
                paginationProvider,
                pagination)
            .mapWithLookup(
                BookmarkedPostDto::authorId,
                userAssembler::toUserSummaries,
                (post, authors) ->
                    postBookmarkAssembler.toBookmarkedPostResponse(
                        post, authors.get(post.authorId())));

    return new GetBookmarkedPostsResponse(bookmarkedPosts);
  }
}
