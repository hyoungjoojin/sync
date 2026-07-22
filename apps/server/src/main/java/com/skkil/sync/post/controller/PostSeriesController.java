package com.skkil.sync.post.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.post.dto.request.AddPostToPostSeriesRequest;
import com.skkil.sync.post.dto.request.CreatePostSeriesRequest;
import com.skkil.sync.post.dto.request.ReorderPostSeriesPostRequest;
import com.skkil.sync.post.dto.request.UpdatePostSeriesRequest;
import com.skkil.sync.post.dto.response.CreatePostSeriesResponse;
import com.skkil.sync.post.dto.response.GetPostSeriesListResponse;
import com.skkil.sync.post.dto.response.GetPostSeriesResponse;
import com.skkil.sync.post.service.PostSeriesService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostSeriesController {

  private final PostSeriesService seriesService;

  public PostSeriesController(PostSeriesService seriesService) {
    this.seriesService = seriesService;
  }

  @PostMapping("/series")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatePostSeriesResponse createSeries(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestBody @Validated CreatePostSeriesRequest request) {
    return seriesService.createPersonalSeries(user.userId(), request);
  }

  @PostMapping("/projects/{handle}/series")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatePostSeriesResponse createProjectSeries(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable String handle,
      @RequestBody @Validated CreatePostSeriesRequest request) {
    return seriesService.createProjectSeries(user.userId(), handle, request);
  }

  @GetMapping("/me/series")
  public GetPostSeriesListResponse getMyPersonalSeries(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return seriesService.getMyPersonalSeries(user.userId());
  }

  @GetMapping("/projects/{handle}/series")
  public GetPostSeriesListResponse getMyProjectSeries(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String handle) {
    return seriesService.getMyProjectSeries(user.userId(), handle);
  }

  @GetMapping("/posts/{slug}/series")
  public GetPostSeriesResponse getSeriesForPost(
      @AuthenticationPrincipal @Nullable AuthenticatedUser user, @PathVariable String slug) {
    return seriesService.getSeriesForPost(slug, user);
  }

  @PatchMapping("/series/{externalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateSeries(
      @PathVariable String externalId, @RequestBody @Validated UpdatePostSeriesRequest request) {
    seriesService.updateSeries(externalId, request);
  }

  @DeleteMapping("/series/{externalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSeries(@PathVariable String externalId) {
    seriesService.deleteSeries(externalId);
  }

  @PostMapping("/series/{externalId}/posts")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addPost(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable String externalId,
      @RequestBody @Validated AddPostToPostSeriesRequest request) {
    seriesService.addPost(user.userId(), externalId, request);
  }

  @PatchMapping("/series/{externalId}/posts/{seriesPostId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reorderPost(
      @PathVariable String externalId,
      @PathVariable Long seriesPostId,
      @RequestBody @Validated ReorderPostSeriesPostRequest request) {
    seriesService.reorderPost(externalId, seriesPostId, request);
  }

  @DeleteMapping("/series/{externalId}/posts/{seriesPostId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(@PathVariable String externalId, @PathVariable Long seriesPostId) {
    seriesService.removeItem(externalId, seriesPostId);
  }
}
