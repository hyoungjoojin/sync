package com.skkil.sync.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.exception.PostReferenceLimitExceededException;
import com.skkil.sync.post.exception.PostReferenceSelfException;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostReference;
import com.skkil.sync.post.repository.PostReferenceRepository;
import com.skkil.sync.post.repository.PostRepository;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostReferenceServiceTests {

  @Mock private PostReferenceRepository postReferenceRepository;

  @Mock private PostRepository postRepository;

  @InjectMocks private PostReferenceService postReferenceService;

  @Captor private ArgumentCaptor<List<PostReference>> referencesCaptor;

  private static Post post(long id) {
    Post post = Post.builder().slug("slug-" + id).build();
    post.setId(id);
    return post;
  }

  @Test
  @DisplayName("[replaceReferences] 참조 목록이 null이면 기존 참조만 삭제하고 새로 저장하지 않음")
  void replaceReferences_null_deletesOnly() {
    Post source = post(1L);

    postReferenceService.replaceReferences(source, null);

    verify(postReferenceRepository).deleteBySourcePostId(1L);
    verify(postReferenceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("[replaceReferences] 빈 목록이면 기존 참조만 삭제하고 새로 저장하지 않음")
  void replaceReferences_empty_deletesOnly() {
    Post source = post(1L);

    postReferenceService.replaceReferences(source, List.of());

    verify(postReferenceRepository).deleteBySourcePostId(1L);
    verify(postReferenceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("[replaceReferences] 자기 자신을 참조하면 PostReferenceSelfException 예외 발생")
  void replaceReferences_self_throws() {
    Post source = post(1L);

    assertThatThrownBy(() -> postReferenceService.replaceReferences(source, List.of(2L, 1L)))
        .isInstanceOf(PostReferenceSelfException.class);
    verify(postReferenceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("[replaceReferences] 참조 개수가 최대치를 초과하면 PostReferenceLimitExceededException 예외 발생")
  void replaceReferences_overLimit_throws() {
    Post source = post(1L);
    List<Long> ids = LongStream.rangeClosed(100L, 150L).boxed().toList();

    assertThatThrownBy(() -> postReferenceService.replaceReferences(source, ids))
        .isInstanceOf(PostReferenceLimitExceededException.class);
    verify(postReferenceRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("[replaceReferences] 존재하지 않는 게시글을 참조하면 PostNotFoundException 예외 발생")
  void replaceReferences_referencedPostNotFound_throws() {
    Post source = post(1L);

    when(postRepository.findAllById(any())).thenReturn(List.of(post(100L)));

    assertThatThrownBy(() -> postReferenceService.replaceReferences(source, List.of(100L, 101L)))
        .isInstanceOf(PostNotFoundException.class);
  }

  @Test
  @DisplayName("[replaceReferences] 중복은 첫 등장 순서로 제거하고 요청 순서대로 sortOrder를 부여해 저장")
  void replaceReferences_dedupsAndOrders() {
    Post source = post(1L);

    when(postRepository.findAllById(any())).thenReturn(List.of(post(100L), post(101L)));

    postReferenceService.replaceReferences(source, List.of(100L, 100L, 101L));

    verify(postReferenceRepository).deleteBySourcePostId(1L);
    verify(postReferenceRepository).saveAll(referencesCaptor.capture());

    List<PostReference> saved = referencesCaptor.getValue();
    assertThat(saved).hasSize(2);
    assertThat(saved)
        .extracting(ref -> ref.getReferencedPost().getId())
        .containsExactly(100L, 101L);
    assertThat(saved).extracting(PostReference::getSortOrder).containsExactly(0, 1);
    assertThat(saved).allSatisfy(ref -> assertThat(ref.getSourcePost()).isSameAs(source));
  }
}
