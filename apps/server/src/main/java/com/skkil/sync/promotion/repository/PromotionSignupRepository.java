package com.skkil.sync.promotion.repository;

import com.skkil.sync.promotion.model.PromotionSignup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionSignupRepository extends JpaRepository<PromotionSignup, Long> {

  Optional<PromotionSignup> findByPromotionIdAndUserId(Long promotionId, Long userId);

  List<PromotionSignup> findByUserIdAndPromotionIdIn(Long userId, List<Long> promotionIds);

  List<PromotionSignup> findByPromotionIdOrderByCreatedAtDesc(Long promotionId);
}
