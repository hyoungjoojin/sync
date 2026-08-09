package com.skkil.sync.promotion.repository;

import com.skkil.sync.promotion.model.Promotion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  List<Promotion> findByActiveTrue();
}
