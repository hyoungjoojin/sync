package com.skkil.sync.promotion.model;

import com.skkil.sync.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "promotion_signups")
@Getter
public class PromotionSignup extends BaseEntity {

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "attachment", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> attachment;

  protected PromotionSignup() {}

  @Builder
  public PromotionSignup(Long promotionId, Long userId, Map<String, String> attachment) {
    this.promotionId = promotionId;
    this.userId = userId;
    this.attachment = attachment;
  }

  public void updateAttachment(Map<String, String> attachment) {
    this.attachment = attachment;
  }
}
