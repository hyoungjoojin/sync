package com.skkil.sync.promotion.service;

import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.promotion.dto.request.CreatePromotionRequest;
import com.skkil.sync.promotion.dto.request.PromotionFieldRequest;
import com.skkil.sync.promotion.dto.request.UpdatePromotionRequest;
import com.skkil.sync.promotion.dto.response.ActivePromotionResponse;
import com.skkil.sync.promotion.dto.response.PromotionFieldResponse;
import com.skkil.sync.promotion.dto.response.PromotionResponse;
import com.skkil.sync.promotion.dto.response.PromotionSignupResponse;
import com.skkil.sync.promotion.exception.PromotionNotFoundException;
import com.skkil.sync.promotion.model.Promotion;
import com.skkil.sync.promotion.model.PromotionField;
import com.skkil.sync.promotion.model.PromotionSignup;
import com.skkil.sync.promotion.repository.PromotionFieldRepository;
import com.skkil.sync.promotion.repository.PromotionRepository;
import com.skkil.sync.promotion.repository.PromotionSignupRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionFieldRepository promotionFieldRepository;
  private final PromotionSignupRepository promotionSignupRepository;
  private final PostDomainService postDomainService;

  public PromotionService(
      PromotionRepository promotionRepository,
      PromotionFieldRepository promotionFieldRepository,
      PromotionSignupRepository promotionSignupRepository,
      PostDomainService postDomainService) {
    this.promotionRepository = promotionRepository;
    this.promotionFieldRepository = promotionFieldRepository;
    this.promotionSignupRepository = promotionSignupRepository;
    this.postDomainService = postDomainService;
  }

  @Transactional(readOnly = true)
  public List<PromotionResponse> getAll() {
    List<Promotion> promotions = promotionRepository.findAll();
    if (promotions.isEmpty()) {
      return List.of();
    }

    Map<Long, List<PromotionField>> fieldsByPromotionId = fieldsByPromotionId(promotions);
    return promotions.stream()
        .map(
            promotion ->
                toResponse(
                    promotion, fieldsByPromotionId.getOrDefault(promotion.getId(), List.of())))
        .toList();
  }

  @Transactional
  public PromotionResponse create(CreatePromotionRequest request) {
    validatePostReference(request.postSlug());

    Promotion promotion =
        promotionRepository.save(
            Promotion.builder()
                .projectHandle(request.projectHandle())
                .postSlug(request.postSlug())
                .build());
    List<PromotionField> fields = saveFields(promotion.getId(), request.fields());
    return toResponse(promotion, fields);
  }

  @Transactional
  public PromotionResponse update(Long id, UpdatePromotionRequest request) {
    validatePostReference(request.postSlug());

    Promotion promotion = getPromotion(id);
    promotion.update(request.projectHandle(), request.postSlug());

    promotionFieldRepository.deleteByPromotionId(id);
    List<PromotionField> fields = saveFields(id, request.fields());

    return toResponse(promotion, fields);
  }

  @Transactional
  public PromotionResponse activate(Long id) {
    Promotion promotion = getPromotion(id);
    promotion.activate();
    return toResponse(promotion, promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(id));
  }

  @Transactional
  public PromotionResponse deactivate(Long id) {
    Promotion promotion = getPromotion(id);
    promotion.deactivate();
    return toResponse(promotion, promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(id));
  }

  @Transactional(readOnly = true)
  public List<ActivePromotionResponse> getActiveForUser(Long userId) {
    List<Promotion> activePromotions = promotionRepository.findByActiveTrue();
    if (activePromotions.isEmpty()) {
      return List.of();
    }

    List<Long> promotionIds = activePromotions.stream().map(Promotion::getId).toList();
    Map<Long, PromotionSignup> signupsByPromotionId =
        promotionSignupRepository.findByUserIdAndPromotionIdIn(userId, promotionIds).stream()
            .collect(Collectors.toMap(PromotionSignup::getPromotionId, signup -> signup));
    Map<Long, List<PromotionField>> fieldsByPromotionId = fieldsByPromotionId(activePromotions);

    return activePromotions.stream()
        .map(
            promotion -> {
              PromotionSignup signup = signupsByPromotionId.get(promotion.getId());
              PromotionSignupResponse signupResponse =
                  signup == null
                      ? null
                      : new PromotionSignupResponse(signup.getAttachment(), signup.getCreatedAt());
              return new ActivePromotionResponse(
                  promotion.getId(),
                  toFieldResponses(fieldsByPromotionId.getOrDefault(promotion.getId(), List.of())),
                  promotion.getProjectHandle(),
                  promotion.getPostSlug(),
                  signupResponse);
            })
        .toList();
  }

  private Promotion getPromotion(Long id) {
    return promotionRepository.findById(id).orElseThrow(() -> new PromotionNotFoundException(id));
  }

  // Every promotion links to a post — the client always creates it first and only calls
  // this with a slug that already exists. This just guards against a stale/typo'd slug
  // producing a dead-linked promotion.
  private void validatePostReference(String postSlug) {
    postDomainService.getPostBySlug(postSlug);
  }

  private List<PromotionField> saveFields(Long promotionId, List<PromotionFieldRequest> requests) {
    List<PromotionField> fields = new ArrayList<>();
    for (int index = 0; index < requests.size(); index++) {
      PromotionFieldRequest request = requests.get(index);
      fields.add(
          PromotionField.builder()
              .promotionId(promotionId)
              .key(request.key())
              .type(request.type())
              .label(request.label())
              .required(request.required())
              .sortOrder(index)
              .build());
    }

    return promotionFieldRepository.saveAll(fields);
  }

  private Map<Long, List<PromotionField>> fieldsByPromotionId(List<Promotion> promotions) {
    List<Long> promotionIds = promotions.stream().map(Promotion::getId).toList();
    return promotionFieldRepository.findByPromotionIdInOrderBySortOrderAsc(promotionIds).stream()
        .collect(Collectors.groupingBy(PromotionField::getPromotionId));
  }

  private static List<PromotionFieldResponse> toFieldResponses(List<PromotionField> fields) {
    return fields.stream()
        .map(
            field ->
                new PromotionFieldResponse(
                    field.getKey(), field.getType(), field.getLabel(), field.isRequired()))
        .toList();
  }

  // The admin list has no title of its own to show — it displays the linked post's title
  // instead, fetched here rather than duplicated into a stored column.
  private @Nullable String postTitle(Promotion promotion) {
    if (promotion.getPostSlug() == null) {
      return null;
    }
    return postDomainService.getPostBySlug(promotion.getPostSlug()).getTitle();
  }

  private PromotionResponse toResponse(Promotion promotion, List<PromotionField> fields) {
    return new PromotionResponse(
        promotion.getId(),
        promotion.isActive(),
        toFieldResponses(fields),
        promotion.getProjectHandle(),
        promotion.getPostSlug(),
        postTitle(promotion),
        promotion.getCreatedAt());
  }
}
