package com.skkil.sync.promotion.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.promotion.dto.request.CreatePromotionSignupRequest;
import com.skkil.sync.promotion.dto.response.ActivePromotionResponse;
import com.skkil.sync.promotion.dto.response.PromotionSignupResponse;
import com.skkil.sync.promotion.service.PromotionService;
import com.skkil.sync.promotion.service.PromotionSignupService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PromotionController {

  private final PromotionService promotionService;
  private final PromotionSignupService promotionSignupService;

  public PromotionController(
      PromotionService promotionService, PromotionSignupService promotionSignupService) {
    this.promotionService = promotionService;
    this.promotionSignupService = promotionSignupService;
  }

  @GetMapping("/promotions/active")
  @ResponseStatus(HttpStatus.OK)
  public List<ActivePromotionResponse> getActivePromotions(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return promotionService.getActiveForUser(user.userId());
  }

  @PostMapping("/promotions/{promotionId}/signups")
  @ResponseStatus(HttpStatus.OK)
  public PromotionSignupResponse submitSignup(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable Long promotionId,
      @RequestBody @Validated CreatePromotionSignupRequest request) {
    return promotionSignupService.submit(user.userId(), promotionId, request);
  }
}
