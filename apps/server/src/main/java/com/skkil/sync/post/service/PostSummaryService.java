package com.skkil.sync.post.service;

import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.service.PaginationService;
import com.skkil.sync.post.dto.response.GetSummariesResponse;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.mapper.PostSummaryMapper;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.post.repository.PostSummaryQueryRepository;
import com.skkil.sync.post.repository.pagination.SummaryCursorPaginationProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostSummaryService {

  private final PostSummaryQueryRepository postSummaryQueryRepository;
  private final PostSummaryMapper postSummaryMapper;
  private final SummaryCursorPaginationProvider paginationProvider;
  private final PaginationService paginationService;
  private final PostRepository postRepository;

  public PostSummaryService(
      PostSummaryQueryRepository postSummaryQueryRepository,
      PostSummaryMapper postSummaryMapper,
      SummaryCursorPaginationProvider paginationProvider,
      PaginationService paginationService,
      PostRepository postRepository) {
    this.postSummaryQueryRepository = postSummaryQueryRepository;
    this.postSummaryMapper = postSummaryMapper;
    this.paginationProvider = paginationProvider;
    this.paginationService = paginationService;
    this.postRepository = postRepository;
  }

  @Transactional(readOnly = true)
  public GetSummariesResponse getSummaries(Long authorId, CursorPaginationRequest pagination) {
    var summaries =
        paginationService
            .paginate(
                postSummaryQueryRepository.getSummaries(authorId), paginationProvider, pagination)
            .map(postSummaryMapper::toSummaryResponse);

    return new GetSummariesResponse(summaries);
  }

  /**
   * Persists a system-generated summary (e.g. from the AI summarization listener). Unlike {@link
   * PostService#updatePostSummary}, this is not a user-initiated edit, so it is not gated behind
   * the 'EDIT' permission.
   */
  @Transactional
  public void updateGeneratedSummary(Long postId, String summary) {
    Post post =
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

    post.updateSummary(summary);
  }
}
