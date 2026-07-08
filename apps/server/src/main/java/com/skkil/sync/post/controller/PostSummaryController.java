package com.skkil.sync.post.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.post.dto.response.GetSummariesResponse;
import com.skkil.sync.post.service.PostSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostSummaryController {

  private final PostSummaryService postSummaryService;

  public PostSummaryController(PostSummaryService postSummaryService) {
    this.postSummaryService = postSummaryService;
  }

  @GetMapping("/summaries")
  @ResponseStatus(HttpStatus.OK)
  public GetSummariesResponse getSummaries(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Validated CursorPaginationRequest pagination) {
    return postSummaryService.getSummaries(user.userId(), pagination);
  }
}
