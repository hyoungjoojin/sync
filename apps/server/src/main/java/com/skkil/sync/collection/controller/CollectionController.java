package com.skkil.sync.collection.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.collection.dto.request.AddPostToCollectionRequest;
import com.skkil.sync.collection.dto.request.CreateCollectionRequest;
import com.skkil.sync.collection.dto.request.UpdateCollectionRequest;
import com.skkil.sync.collection.dto.response.CreateCollectionResponse;
import com.skkil.sync.collection.dto.response.GetCollectionPostsResponse;
import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.service.CollectionService;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectionController {

  private final CollectionService collectionService;

  public CollectionController(CollectionService collectionService) {
    this.collectionService = collectionService;
  }

  @PostMapping("/collections")
  @ResponseStatus(HttpStatus.CREATED)
  public CreateCollectionResponse createCollection(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestBody @Validated CreateCollectionRequest request) {
    return collectionService.createPersonalCollection(user.userId(), request);
  }

  @PostMapping("/projects/{handle}/collections")
  @ResponseStatus(HttpStatus.CREATED)
  public CreateCollectionResponse createProjectCollection(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable String handle,
      @RequestBody @Validated CreateCollectionRequest request) {
    return collectionService.createProjectCollection(user.userId(), handle, request);
  }

  @GetMapping("/users/{userId}/collections")
  public GetCollectionsResponse getUserCollections(
      @AuthenticationPrincipal @Nullable AuthenticatedUser user,
      @PathVariable Long userId,
      @RequestParam(required = false) @Nullable String postHandle) {
    return collectionService.getUserCollections(
        user == null ? null : user.userId(), userId, postHandle);
  }

  @GetMapping("/projects/{handle}/collections")
  public GetCollectionsResponse getProjectCollections(
      @AuthenticationPrincipal @Nullable AuthenticatedUser user,
      @PathVariable String handle,
      @RequestParam(required = false) @Nullable String postHandle) {
    return collectionService.getProjectCollections(
        user == null ? null : user.userId(), handle, postHandle);
  }

  @GetMapping("/collections/{externalId}")
  public CollectionSummary getCollection(@PathVariable String externalId) {
    return collectionService.getCollection(externalId);
  }

  @GetMapping("/collections/{externalId}/posts")
  public GetCollectionPostsResponse getCollectionPosts(
      @AuthenticationPrincipal @Nullable AuthenticatedUser user,
      @PathVariable String externalId,
      @Validated CursorPaginationRequest pagination) {
    return collectionService.getCollectionPosts(externalId, user, pagination);
  }

  @PatchMapping("/collections/{externalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateCollection(
      @PathVariable String externalId, @RequestBody @Validated UpdateCollectionRequest request) {
    collectionService.updateCollection(externalId, request);
  }

  @DeleteMapping("/collections/{externalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCollection(@PathVariable String externalId) {
    collectionService.deleteCollection(externalId);
  }

  @PostMapping("/collections/{externalId}/posts")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addPost(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable String externalId,
      @RequestBody @Validated AddPostToCollectionRequest request) {
    collectionService.addPost(user.userId(), externalId, request);
  }

  @DeleteMapping("/collections/{externalId}/items/{collectionPostId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeItem(@PathVariable String externalId, @PathVariable Long collectionPostId) {
    collectionService.removeItem(externalId, collectionPostId);
  }
}
