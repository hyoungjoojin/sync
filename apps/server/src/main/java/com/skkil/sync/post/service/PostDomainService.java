package com.skkil.sync.post.service;

import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.mapper.PostAssembler;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostVisibility;
import com.skkil.sync.post.repository.PostQueryRepository;
import com.skkil.sync.post.repository.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostDomainService {

  private final PostRepository postRepository;
  private final PostQueryRepository postQueryRepository;
  private final PostAssembler postAssembler;

  public PostDomainService(
      PostRepository postRepository,
      PostQueryRepository postQueryRepository,
      PostAssembler postAssembler) {
    this.postRepository = postRepository;
    this.postQueryRepository = postQueryRepository;
    this.postAssembler = postAssembler;
  }

  @Transactional(readOnly = true)
  public Post getPost(Long postId) {
    return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
  }

  @Transactional(readOnly = true)
  public Post getPostBySlug(String slug) {
    return postRepository.findBySlug(slug).orElseThrow(() -> new PostNotFoundException(slug));
  }

  @Transactional(readOnly = true)
  public Post getPublicPublishedPost(Long postId) {
    return postRepository
        .findByIdAndVisibilityAndStatusAndProjectIsNull(
            postId, PostVisibility.VISIBLE, PostStatus.PUBLISHED)
        .orElseThrow(() -> new PostNotFoundException(postId));
  }

  @Transactional(readOnly = true)
  public Post getPublicPublishedPostBySlug(String slug) {
    return postRepository
        .findBySlugAndVisibilityAndStatusAndProjectIsNull(
            slug, PostVisibility.VISIBLE, PostStatus.PUBLISHED)
        .orElseThrow(() -> new PostNotFoundException(slug));
  }

  @Transactional(readOnly = true)
  public Map<Long, PostSummary> getReadablePostSummaries(
      @Nullable Long requesterId, List<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    List<PostDto> dtos = postQueryRepository.getPostsByIds(requesterId, postIds);
    return postAssembler.toPostResponses(dtos, requesterId).stream()
        .collect(Collectors.toMap(PostSummary::id, summary -> summary));
  }

  @Transactional(readOnly = true)
  public boolean isPostReadable(@Nullable Long requesterId, Long postId) {
    return !postQueryRepository.getPostsByIds(requesterId, List.of(postId)).isEmpty();
  }
}
