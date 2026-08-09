package com.skkil.sync.promotion.repository;

import com.skkil.sync.promotion.model.PromotionField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PromotionFieldRepository extends JpaRepository<PromotionField, Long> {

  List<PromotionField> findByPromotionIdOrderBySortOrderAsc(Long promotionId);

  List<PromotionField> findByPromotionIdInOrderBySortOrderAsc(List<Long> promotionIds);

  // A derived delete here would only queue entity removals in the persistence context —
  // Hibernate flushes inserts before deletes, so the replacement fields saveFields() inserts
  // right after this call would hit the (promotion_id, field_key) unique constraint before
  // the old rows were actually gone. A @Modifying query executes immediately instead.
  @Modifying
  @Query("DELETE FROM PromotionField f WHERE f.promotionId = :promotionId")
  void deleteByPromotionId(Long promotionId);
}
