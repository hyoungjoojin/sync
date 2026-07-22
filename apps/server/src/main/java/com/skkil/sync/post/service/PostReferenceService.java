package com.skkil.sync.post.service;

import com.skkil.sync.post.constants.PostConstants;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.exception.PostReferenceLimitExceededException;
import com.skkil.sync.post.exception.PostReferenceSelfException;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostReference;
import com.skkil.sync.post.repository.PostReferenceRepository;
import com.skkil.sync.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글이 가리키는 참조(forward reference) 목록을 관리한다. 참조는 게시글 편집의 일부이므로 별도 권한이 없으며, 호출부(게시글 생성/수정)가 이미 {@code
 * POST EDIT} 권한을 확인했다는 전제 아래 동작한다. 대상 게시글의 열람 가능 여부는 쓰기 시점에 검증하지 않고(어떤 게시글이든 참조로 붙일 수 있다) 조회 시점에
 * 해석한다 — 컬렉션과 동일한 정책.
 */
@Service
public class PostReferenceService {

  private final PostReferenceRepository postReferenceRepository;
  private final PostRepository postRepository;

  public PostReferenceService(
      PostReferenceRepository postReferenceRepository, PostRepository postRepository) {
    this.postReferenceRepository = postReferenceRepository;
    this.postRepository = postRepository;
  }

  /**
   * {@code source} 의 참조 목록을 요청된 순서대로 전면 교체한다. 중복 id 는 첫 등장 순서를 유지하며 제거하고, 자기 자신 참조·개수 초과·존재하지 않는
   * 게시글은 예외로 막는다.
   */
  @Transactional
  public void replaceReferences(Post source, @Nullable List<Long> referencedPostIds) {
    postReferenceRepository.deleteBySourcePostId(source.getId());

    if (referencedPostIds == null || referencedPostIds.isEmpty()) {
      return;
    }

    List<Long> orderedIds = new ArrayList<>(new LinkedHashSet<>(referencedPostIds));

    if (orderedIds.contains(source.getId())) {
      throw new PostReferenceSelfException();
    }
    if (orderedIds.size() > PostConstants.MAX_REFERENCES_PER_POST) {
      throw new PostReferenceLimitExceededException();
    }

    Map<Long, Post> referencedById =
        postRepository.findAllById(orderedIds).stream()
            .collect(Collectors.toMap(Post::getId, Function.identity()));

    List<PostReference> references = new ArrayList<>(orderedIds.size());
    for (int i = 0; i < orderedIds.size(); i++) {
      Long referencedId = orderedIds.get(i);
      Post referenced = referencedById.get(referencedId);
      if (referenced == null) {
        throw new PostNotFoundException(referencedId);
      }
      references.add(
          PostReference.builder()
              .sourcePost(source)
              .referencedPost(referenced)
              .sortOrder(i)
              .build());
    }

    postReferenceRepository.saveAll(references);
  }
}
