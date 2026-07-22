package com.skkil.sync.post.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.post.dto.response.GetPostsResponse;
import com.skkil.sync.post.service.RelatedPostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RelatedPostController {

  private final RelatedPostService relatedPostService;

  public RelatedPostController(RelatedPostService relatedPostService) {
    this.relatedPostService = relatedPostService;
  }

  @GetMapping("/posts/{postId}/related")
  @ResponseStatus(HttpStatus.OK)
  public GetPostsResponse getRelatedPosts(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long postId) {
    return relatedPostService.getRelatedPosts(user == null ? null : user.userId(), postId);
  }
}
