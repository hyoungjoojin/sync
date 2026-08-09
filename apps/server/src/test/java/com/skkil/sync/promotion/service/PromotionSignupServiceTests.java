package com.skkil.sync.promotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skkil.sync.promotion.dto.request.CreatePromotionSignupRequest;
import com.skkil.sync.promotion.dto.response.AdminPromotionSignupResponse;
import com.skkil.sync.promotion.exception.InvalidPromotionSignupAttachmentException;
import com.skkil.sync.promotion.exception.PromotionNotFoundException;
import com.skkil.sync.promotion.model.Promotion;
import com.skkil.sync.promotion.model.PromotionField;
import com.skkil.sync.promotion.model.PromotionFieldType;
import com.skkil.sync.promotion.model.PromotionSignup;
import com.skkil.sync.promotion.repository.PromotionFieldRepository;
import com.skkil.sync.promotion.repository.PromotionRepository;
import com.skkil.sync.promotion.repository.PromotionSignupRepository;
import com.skkil.sync.user.dto.summary.UserSummary;
import com.skkil.sync.user.mapper.UserAssembler;
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
class PromotionSignupServiceTests {

  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionFieldRepository promotionFieldRepository;
  @Mock private PromotionSignupRepository promotionSignupRepository;
  @Mock private UserAssembler userAssembler;

  private PromotionSignupService promotionSignupService;

  @BeforeEach
  void setUp() {
    promotionSignupService =
        new PromotionSignupService(
            promotionRepository,
            promotionFieldRepository,
            promotionSignupRepository,
            userAssembler);
  }

  private static PromotionField phoneNumberField(boolean required) {
    return PromotionField.builder()
        .promotionId(1L)
        .key("phoneNumber")
        .type(PromotionFieldType.PHONE_NUMBER)
        .label("전화번호")
        .required(required)
        .sortOrder(0)
        .build();
  }

  private void givenActivePromotionWithFields(Long promotionId, List<PromotionField> fields) {
    Promotion promotion = Promotion.builder().build();
    promotion.setId(promotionId);
    promotion.activate();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(promotionId))
        .thenReturn(fields);
  }

  @Test
  @DisplayName("[submit] 존재하지 않는 프로모션이면 PromotionNotFoundException 발생")
  void submit_promotionNotFound_throwsPromotionNotFoundException() {
    Long promotionId = 1L;
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L,
                    promotionId,
                    new CreatePromotionSignupRequest(Map.of("phoneNumber", "01012345678"))))
        .isInstanceOf(PromotionNotFoundException.class);
  }

  @Test
  @DisplayName("[submit] 비활성 프로모션이면 PromotionNotFoundException 발생")
  void submit_inactivePromotion_throwsPromotionNotFoundException() {
    Long promotionId = 1L;
    Promotion promotion = Promotion.builder().build();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L,
                    promotionId,
                    new CreatePromotionSignupRequest(Map.of("phoneNumber", "01012345678"))))
        .isInstanceOf(PromotionNotFoundException.class);
  }

  @Test
  @DisplayName("[submit] 프로모션에 등록되지 않은 키를 제출하면 InvalidPromotionSignupAttachmentException 발생")
  void submit_unknownKey_throwsInvalidPromotionSignupAttachmentException() {
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(true)));

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L, promotionId, new CreatePromotionSignupRequest(Map.of("email", "a@b.com"))))
        .isInstanceOf(InvalidPromotionSignupAttachmentException.class);
  }

  @Test
  @DisplayName("[submit] 필수 필드를 제출하지 않으면 InvalidPromotionSignupAttachmentException 발생")
  void submit_missingRequiredField_throwsInvalidPromotionSignupAttachmentException() {
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(true)));

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L, promotionId, new CreatePromotionSignupRequest(Map.of())))
        .isInstanceOf(InvalidPromotionSignupAttachmentException.class);
  }

  @Test
  @DisplayName("[submit] 선택 필드를 제출하지 않아도 통과한다")
  void submit_missingOptionalField_succeeds() {
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(false)));
    when(promotionSignupRepository.findByPromotionIdAndUserId(promotionId, 1L))
        .thenReturn(Optional.empty());
    when(promotionSignupRepository.save(any(PromotionSignup.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        promotionSignupService.submit(1L, promotionId, new CreatePromotionSignupRequest(Map.of()));

    assertThat(response.attachment()).isEmpty();
  }

  @Test
  @DisplayName("[submit] PHONE_NUMBER 타입 필드에 형식이 다른 값을 제출하면 예외 발생")
  void submit_invalidPhoneNumberFormat_throwsInvalidPromotionSignupAttachmentException() {
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(true)));

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L,
                    promotionId,
                    new CreatePromotionSignupRequest(Map.of("phoneNumber", "not-a-phone"))))
        .isInstanceOf(InvalidPromotionSignupAttachmentException.class);
  }

  @Test
  @DisplayName("[submit] CHECKBOX 타입 필드는 true/false 문자열만 허용한다")
  void submit_invalidCheckboxValue_throwsInvalidPromotionSignupAttachmentException() {
    Long promotionId = 1L;
    PromotionField checkbox =
        PromotionField.builder()
            .promotionId(promotionId)
            .key("agreed")
            .type(PromotionFieldType.CHECKBOX)
            .label("동의")
            .required(true)
            .sortOrder(0)
            .build();
    givenActivePromotionWithFields(promotionId, List.of(checkbox));

    assertThatThrownBy(
            () ->
                promotionSignupService.submit(
                    1L, promotionId, new CreatePromotionSignupRequest(Map.of("agreed", "yes"))))
        .isInstanceOf(InvalidPromotionSignupAttachmentException.class);
  }

  @Test
  @DisplayName("[submit] 신청 이력이 없으면 새로 생성한다")
  void submit_noExistingSignup_createsNewSignup() {
    Long userId = 1L;
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(true)));
    when(promotionSignupRepository.findByPromotionIdAndUserId(promotionId, userId))
        .thenReturn(Optional.empty());
    when(promotionSignupRepository.save(any(PromotionSignup.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        promotionSignupService.submit(
            userId,
            promotionId,
            new CreatePromotionSignupRequest(Map.of("phoneNumber", "01012345678")));

    assertThat(response.attachment()).containsEntry("phoneNumber", "01012345678");
  }

  @Test
  @DisplayName("[submit] 이미 신청한 경우 첨부 데이터를 갱신한다")
  void submit_existingSignup_updatesAttachment() {
    Long userId = 1L;
    Long promotionId = 1L;
    givenActivePromotionWithFields(promotionId, List.of(phoneNumberField(true)));

    PromotionSignup existing =
        PromotionSignup.builder()
            .promotionId(promotionId)
            .userId(userId)
            .attachment(Map.of("phoneNumber", "01000000000"))
            .build();

    when(promotionSignupRepository.findByPromotionIdAndUserId(promotionId, userId))
        .thenReturn(Optional.of(existing));

    var response =
        promotionSignupService.submit(
            userId,
            promotionId,
            new CreatePromotionSignupRequest(Map.of("phoneNumber", "01012345678")));

    assertThat(response.attachment()).containsEntry("phoneNumber", "01012345678");
  }

  @Test
  @DisplayName("[getSignups] 존재하지 않는 프로모션이면 PromotionNotFoundException 발생")
  void getSignups_promotionNotFound_throwsPromotionNotFoundException() {
    Long promotionId = 1L;
    when(promotionRepository.existsById(promotionId)).thenReturn(false);

    assertThatThrownBy(() -> promotionSignupService.getSignups(promotionId))
        .isInstanceOf(PromotionNotFoundException.class);
  }

  @Test
  @DisplayName("[getSignups] 신청자 목록을 유저 정보와 함께 반환한다")
  void getSignups_returnsSignupsWithUserSummary() {
    Long promotionId = 1L;
    Long userId = 2L;
    PromotionSignup signup =
        PromotionSignup.builder()
            .promotionId(promotionId)
            .userId(userId)
            .attachment(Map.of("phoneNumber", "01012345678"))
            .build();
    signup.setId(3L);

    UserSummary userSummary = UserSummary.builder().handle("john-doe").name("John Doe").build();

    when(promotionRepository.existsById(promotionId)).thenReturn(true);
    when(promotionSignupRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId))
        .thenReturn(List.of(signup));
    when(userAssembler.toUserSummaries(List.of(userId))).thenReturn(Map.of(userId, userSummary));

    List<AdminPromotionSignupResponse> response = promotionSignupService.getSignups(promotionId);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).user()).isEqualTo(userSummary);
    assertThat(response.get(0).attachment()).containsEntry("phoneNumber", "01012345678");
  }
}
