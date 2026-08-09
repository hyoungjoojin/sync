package com.skkil.sync.post.service;

import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.mapper.PostAssembler;
import com.skkil.sync.post.model.Post;
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

  /**
   * 요청자가 열람할 수 있는 게시글만 돌려준다. 개인 게시글인지 프로젝트 게시글인지에 따른 노출 규칙은 PostConditions.readable 이 단독으로 정의하며,
   * 열람할 수 없는 게시글은 존재 자체를 숨기기 위해 404 로 처리한다.
   */
  @Transactional(readOnly = true)
  public PostDto getReadablePostBySlug(@Nullable Long requesterId, String slug) {
    return postQueryRepository
        .getPostBySlug(requesterId, slug)
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
