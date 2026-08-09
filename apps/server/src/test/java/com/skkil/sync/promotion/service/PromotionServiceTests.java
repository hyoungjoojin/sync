package com.skkil.sync.promotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.promotion.dto.request.CreatePromotionRequest;
import com.skkil.sync.promotion.dto.request.PromotionFieldRequest;
import com.skkil.sync.promotion.dto.request.UpdatePromotionRequest;
import com.skkil.sync.promotion.dto.response.ActivePromotionResponse;
import com.skkil.sync.promotion.exception.PromotionNotFoundException;
import com.skkil.sync.promotion.model.Promotion;
import com.skkil.sync.promotion.model.PromotionField;
import com.skkil.sync.promotion.model.PromotionFieldType;
import com.skkil.sync.promotion.model.PromotionSignup;
import com.skkil.sync.promotion.repository.PromotionFieldRepository;
import com.skkil.sync.promotion.repository.PromotionRepository;
import com.skkil.sync.promotion.repository.PromotionSignupRepository;
import com.skkil.sync.user.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTests {

  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionFieldRepository promotionFieldRepository;
  @Mock private PromotionSignupRepository promotionSignupRepository;
  @Mock private PostDomainService postDomainService;

  private PromotionService promotionService;

  @BeforeEach
  void setUp() {
    promotionService =
        new PromotionService(
            promotionRepository,
            promotionFieldRepository,
            promotionSignupRepository,
            postDomainService);
  }

  private static Post linkedPost(String slug) {
    return Post.builder()
        .slug(slug)
        .author(new User(1L))
        .jsonContent("{}")
        .type(PostType.SHORT)
        .build();
  }

  @Test
  @DisplayName("[create] 프로모션은 비활성 상태로 생성되고, 연결된 게시글과 요청한 필드를 저장한다")
  void create_createsInactivePromotionLinkedToPost() {
    CreatePromotionRequest request =
        new CreatePromotionRequest(
            List.of(
                new PromotionFieldRequest(
                    "phoneNumber", PromotionFieldType.PHONE_NUMBER, "전화번호", true)),
            "my-project",
            "post-slug");
    when(postDomainService.getPostBySlug("post-slug")).thenReturn(linkedPost("post-slug"));
    when(promotionRepository.save(any(Promotion.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(promotionFieldRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = promotionService.create(request);

    assertThat(response.active()).isFalse();
    assertThat(response.projectHandle()).isEqualTo("my-project");
    assertThat(response.postSlug()).isEqualTo("post-slug");
    assertThat(response.fields()).hasSize(1);
    assertThat(response.fields().get(0).key()).isEqualTo("phoneNumber");
  }

  @Test
  @DisplayName("[create] 연결할 게시글이 존재하지 않으면 PostNotFoundException 발생")
  void create_withMissingPostReference_throwsPostNotFoundException() {
    CreatePromotionRequest request =
        new CreatePromotionRequest(List.of(), "my-project", "missing-slug");
    when(postDomainService.getPostBySlug("missing-slug"))
        .thenThrow(new PostNotFoundException("missing-slug"));

    assertThatThrownBy(() -> promotionService.create(request))
        .isInstanceOf(PostNotFoundException.class);
  }

  @Test
  @DisplayName("[update] 존재하지 않는 프로모션 수정 시 PromotionNotFoundException 발생")
  void update_promotionNotFound_throwsPromotionNotFoundException() {
    Long promotionId = 1L;
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());
    when(postDomainService.getPostBySlug("post-slug")).thenReturn(linkedPost("post-slug"));

    assertThatThrownBy(
            () ->
                promotionService.update(
                    promotionId, new UpdatePromotionRequest(List.of(), "my-project", "post-slug")))
        .isInstanceOf(PromotionNotFoundException.class);
  }

  @Test
  @DisplayName("[update] 기존 필드를 모두 삭제하고 요청한 필드로 교체한다")
  void update_replacesFields() {
    Long promotionId = 1L;
    Promotion promotion =
        Promotion.builder().projectHandle("my-project").postSlug("post-slug").build();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(postDomainService.getPostBySlug("post-slug")).thenReturn(linkedPost("post-slug"));
    when(promotionFieldRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdatePromotionRequest request =
        new UpdatePromotionRequest(
            List.of(new PromotionFieldRequest("agreed", PromotionFieldType.CHECKBOX, "동의", true)),
            "my-project",
            "post-slug");

    var response = promotionService.update(promotionId, request);

    verify(promotionFieldRepository).deleteByPromotionId(promotionId);
    assertThat(response.fields()).hasSize(1);
    assertThat(response.fields().get(0).key()).isEqualTo("agreed");
  }

  @Test
  @DisplayName("[activate] 프로모션을 활성화한다")
  void activate_activatesPromotion() {
    Long promotionId = 1L;
    Promotion promotion =
        Promotion.builder().projectHandle("my-project").postSlug("post-slug").build();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(promotionId))
        .thenReturn(List.of());
    when(postDomainService.getPostBySlug("post-slug")).thenReturn(linkedPost("post-slug"));

    var response = promotionService.activate(promotionId);

    assertThat(response.active()).isTrue();
  }

  @Test
  @DisplayName("[deactivate] 프로모션을 비활성화한다")
  void deactivate_deactivatesPromotion() {
    Long promotionId = 1L;
    Promotion promotion =
        Promotion.builder().projectHandle("my-project").postSlug("post-slug").build();
    promotion.activate();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(promotionId))
        .thenReturn(List.of());
    when(postDomainService.getPostBySlug("post-slug")).thenReturn(linkedPost("post-slug"));

    var response = promotionService.deactivate(promotionId);

    assertThat(response.active()).isFalse();
  }

  @Test
  @DisplayName("[getActiveForUser] 활성 프로모션이 없으면 빈 리스트를 반환한다")
  void getActiveForUser_noActivePromotions_returnsEmptyList() {
    Long userId = 1L;
    when(promotionRepository.findByActiveTrue()).thenReturn(List.of());

    List<ActivePromotionResponse> response = promotionService.getActiveForUser(userId);

    assertThat(response).isEmpty();
  }

  @Test
  @DisplayName("[getActiveForUser] 사용자의 신청 정보를 활성 프로모션과 결합해 반환한다")
  void getActiveForUser_zipsSignupWithPromotion() {
    Long userId = 1L;
    Promotion signedUp =
        Promotion.builder().projectHandle("my-project").postSlug("post-slug-1").build();
    signedUp.activate();
    setId(signedUp, 1L);
    Promotion notSignedUp =
        Promotion.builder().projectHandle("my-project").postSlug("post-slug-2").build();
    notSignedUp.activate();
    setId(notSignedUp, 2L);

    PromotionSignup signup =
        PromotionSignup.builder()
            .promotionId(1L)
            .userId(userId)
            .attachment(Map.of("phoneNumber", "01012345678"))
            .build();

    when(promotionRepository.findByActiveTrue()).thenReturn(List.of(signedUp, notSignedUp));
    when(promotionSignupRepository.findByUserIdAndPromotionIdIn(eq(userId), any()))
        .thenReturn(List.of(signup));
    when(promotionFieldRepository.findByPromotionIdInOrderBySortOrderAsc(any()))
        .thenReturn(
            List.of(
                PromotionField.builder()
                    .promotionId(1L)
                    .key("phoneNumber")
                    .type(PromotionFieldType.PHONE_NUMBER)
                    .label("전화번호")
                    .required(true)
                    .sortOrder(0)
                    .build()));

    List<ActivePromotionResponse> response = promotionService.getActiveForUser(userId);

    assertThat(response).hasSize(2);
    assertThat(response)
        .filteredOn(r -> r.id().equals(1L))
        .singleElement()
        .satisfies(
            r -> {
              assertThat(r.signup()).isNotNull();
              assertThat(r.fields()).hasSize(1);
              assertThat(r.projectHandle()).isEqualTo("my-project");
              assertThat(r.postSlug()).isEqualTo("post-slug-1");
            });
    assertThat(response)
        .filteredOn(r -> r.id().equals(2L))
        .singleElement()
        .satisfies(r -> assertThat(r.signup()).isNull());
  }

  private static void setId(Promotion promotion, Long id) {
    promotion.setId(id);
  }
}
