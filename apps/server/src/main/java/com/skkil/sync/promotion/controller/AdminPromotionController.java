package com.skkil.sync.promotion.controller;

import com.skkil.sync.promotion.dto.request.CreatePromotionRequest;
import com.skkil.sync.promotion.dto.request.UpdatePromotionRequest;
import com.skkil.sync.promotion.dto.response.AdminPromotionSignupResponse;
import com.skkil.sync.promotion.dto.response.PromotionResponse;
import com.skkil.sync.promotion.service.PromotionService;
import com.skkil.sync.promotion.service.PromotionSignupService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AdminPromotionController {

  private final PromotionService promotionService;
  private final PromotionSignupService promotionSignupService;

  public AdminPromotionController(
      PromotionService promotionService, PromotionSignupService promotionSignupService) {
    this.promotionService = promotionService;
    this.promotionSignupService = promotionSignupService;
  }

  @GetMapping("/admin/promotions")
  @ResponseStatus(HttpStatus.OK)
  public List<PromotionResponse> getPromotions() {
    return promotionService.getAll();
  }

  @PostMapping("/admin/promotions")
  @ResponseStatus(HttpStatus.CREATED)
  public PromotionResponse createPromotion(@RequestBody @Validated CreatePromotionRequest request) {
    return promotionService.create(request);
  }

  @PatchMapping("/admin/promotions/{id}")
  @ResponseStatus(HttpStatus.OK)
  public PromotionResponse updatePromotion(
      @PathVariable Long id, @RequestBody @Validated UpdatePromotionRequest request) {
    return promotionService.update(id, request);
  }

  @PatchMapping("/admin/promotions/{id}/activate")
  @ResponseStatus(HttpStatus.OK)
  public PromotionResponse activatePromotion(@PathVariable Long id) {
    return promotionService.activate(id);
  }

  @PatchMapping("/admin/promotions/{id}/deactivate")
  @ResponseStatus(HttpStatus.OK)
  public PromotionResponse deactivatePromotion(@PathVariable Long id) {
    return promotionService.deactivate(id);
  }

  @GetMapping("/admin/promotions/{id}/signups")
  @ResponseStatus(HttpStatus.OK)
  public List<AdminPromotionSignupResponse> getPromotionSignups(@PathVariable Long id) {
    return promotionSignupService.getSignups(id);
  }
}
