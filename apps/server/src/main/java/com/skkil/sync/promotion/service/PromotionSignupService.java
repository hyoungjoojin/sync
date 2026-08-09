package com.skkil.sync.promotion.service;

import com.skkil.sync.promotion.constant.PromotionConstants;
import com.skkil.sync.promotion.dto.request.CreatePromotionSignupRequest;
import com.skkil.sync.promotion.dto.response.AdminPromotionSignupResponse;
import com.skkil.sync.promotion.dto.response.PromotionSignupResponse;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionSignupService {

  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^01[0-9]{8,9}$");

  private final PromotionRepository promotionRepository;
  private final PromotionFieldRepository promotionFieldRepository;
  private final PromotionSignupRepository promotionSignupRepository;
  private final UserAssembler userAssembler;

  public PromotionSignupService(
      PromotionRepository promotionRepository,
      PromotionFieldRepository promotionFieldRepository,
      PromotionSignupRepository promotionSignupRepository,
      UserAssembler userAssembler) {
    this.promotionRepository = promotionRepository;
    this.promotionFieldRepository = promotionFieldRepository;
    this.promotionSignupRepository = promotionSignupRepository;
    this.userAssembler = userAssembler;
  }

  @Transactional
  public PromotionSignupResponse submit(
      Long userId, Long promotionId, CreatePromotionSignupRequest request) {
    Promotion promotion =
        promotionRepository
            .findById(promotionId)
            .filter(Promotion::isActive)
            .orElseThrow(() -> new PromotionNotFoundException(promotionId));

    List<PromotionField> fields =
        promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(promotionId);
    Map<String, String> attachment = validateAttachment(fields, request.attachment());

    PromotionSignup signup =
        promotionSignupRepository.findByPromotionIdAndUserId(promotionId, userId).orElse(null);

    if (signup == null) {
      signup =
          promotionSignupRepository.save(
              PromotionSignup.builder()
                  .promotionId(promotion.getId())
                  .userId(userId)
                  .attachment(attachment)
                  .build());
    } else {
      signup.updateAttachment(attachment);
    }

    return new PromotionSignupResponse(signup.getAttachment(), signup.getCreatedAt());
  }

  @Transactional(readOnly = true)
  public List<AdminPromotionSignupResponse> getSignups(Long promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new PromotionNotFoundException(promotionId);
    }

    List<PromotionSignup> signups =
        promotionSignupRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId);
    Map<Long, UserSummary> usersById =
        userAssembler.toUserSummaries(signups.stream().map(PromotionSignup::getUserId).toList());

    return signups.stream()
        .map(
            signup ->
                new AdminPromotionSignupResponse(
                    signup.getId(),
                    usersById.get(signup.getUserId()),
                    signup.getAttachment(),
                    signup.getCreatedAt()))
        .toList();
  }

  // Validates the submitted attachment against the promotion's own field definitions —
  // there is no global allow-list, since every promotion declares its own signup shape.
  private static Map<String, String> validateAttachment(
      List<PromotionField> fields, Map<String, String> attachment) {
    if (attachment == null) {
      attachment = Map.of();
    }

    Set<String> allowedKeys =
        fields.stream().map(PromotionField::getKey).collect(Collectors.toSet());
    for (String key : attachment.keySet()) {
      if (!allowedKeys.contains(key)) {
        throw new InvalidPromotionSignupAttachmentException("Unknown attachment key: " + key);
      }
    }

    long totalSize = 0;
    for (PromotionField field : fields) {
      String value = attachment.get(field.getKey());

      if (value == null || value.isBlank()) {
        if (field.isRequired()) {
          throw new InvalidPromotionSignupAttachmentException(
              "Missing required attachment field: " + field.getKey());
        }
        continue;
      }

      if (value.length() > PromotionConstants.MAX_ATTACHMENT_VALUE_LENGTH) {
        throw new InvalidPromotionSignupAttachmentException(
            "Attachment value for " + field.getKey() + " is too long.");
      }
      validateFieldValue(field, value);

      totalSize += field.getKey().getBytes(StandardCharsets.UTF_8).length;
      totalSize += value.getBytes(StandardCharsets.UTF_8).length;
    }

    if (totalSize > PromotionConstants.MAX_ATTACHMENT_SERIALIZED_SIZE_BYTES) {
      throw new InvalidPromotionSignupAttachmentException("Attachment is too large.");
    }

    return attachment;
  }

  private static void validateFieldValue(PromotionField field, String value) {
    if (field.getType() == PromotionFieldType.PHONE_NUMBER
        && !PHONE_NUMBER_PATTERN.matcher(value).matches()) {
      throw new InvalidPromotionSignupAttachmentException(
          "Attachment value for " + field.getKey() + " is not a valid phone number.");
    }

    if (field.getType() == PromotionFieldType.CHECKBOX
        && !("true".equals(value) || "false".equals(value))) {
      throw new InvalidPromotionSignupAttachmentException(
          "Attachment value for " + field.getKey() + " must be true or false.");
    }
  }
}
