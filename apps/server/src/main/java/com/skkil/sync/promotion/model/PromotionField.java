package com.skkil.sync.promotion.model;

import com.skkil.sync.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "promotion_fields")
@Getter
public class PromotionField extends BaseEntity {

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  @Column(name = "field_key", nullable = false, length = 100)
  private String key;

  @Column(name = "field_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private PromotionFieldType type;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "required", nullable = false)
  private boolean required = true;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  protected PromotionField() {}

  @Builder
  public PromotionField(
      Long promotionId,
      String key,
      PromotionFieldType type,
      String label,
      boolean required,
      int sortOrder) {
    this.promotionId = promotionId;
    this.key = key;
    this.type = type;
    this.label = label;
    this.required = required;
    this.sortOrder = sortOrder;
  }
}
