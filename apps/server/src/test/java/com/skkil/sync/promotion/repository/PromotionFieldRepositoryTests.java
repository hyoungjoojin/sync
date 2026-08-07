package com.skkil.sync.promotion.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.skkil.sync.common.config.TestcontainersConfig;
import com.skkil.sync.config.JpaConfig;
import com.skkil.sync.promotion.model.Promotion;
import com.skkil.sync.promotion.model.PromotionField;
import com.skkil.sync.promotion.model.PromotionFieldType;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class})
class PromotionFieldRepositoryTests {

  @Autowired private PromotionRepository promotionRepository;

  @Autowired private PromotionFieldRepository promotionFieldRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("deleteByPromotionId 는 즉시 반영되어, 같은 키로 필드를 다시 저장해도 유니크 제약을 위반하지 않는다")
  void deleteByPromotionId_replacingWithSameKey_doesNotViolateUniqueConstraint() {
    Promotion promotion = promotionRepository.save(Promotion.builder().build());
    promotionFieldRepository.save(
        PromotionField.builder()
            .promotionId(promotion.getId())
            .key("phoneNumber")
            .type(PromotionFieldType.PHONE_NUMBER)
            .label("전화번호")
            .required(true)
            .sortOrder(0)
            .build());
    entityManager.flush();
    entityManager.clear();

    assertThatCode(
            () -> {
              promotionFieldRepository.deleteByPromotionId(promotion.getId());
              promotionFieldRepository.save(
                  PromotionField.builder()
                      .promotionId(promotion.getId())
                      .key("phoneNumber")
                      .type(PromotionFieldType.PHONE_NUMBER)
                      .label("전화번호 (수정됨)")
                      .required(false)
                      .sortOrder(0)
                      .build());
              entityManager.flush();
            })
        .doesNotThrowAnyException();

    entityManager.clear();
    List<PromotionField> fields =
        promotionFieldRepository.findByPromotionIdOrderBySortOrderAsc(promotion.getId());
    assertThat(fields)
        .singleElement()
        .satisfies(
            field -> {
              assertThat(field.getKey()).isEqualTo("phoneNumber");
              assertThat(field.getLabel()).isEqualTo("전화번호 (수정됨)");
              assertThat(field.isRequired()).isFalse();
            });
  }
}
