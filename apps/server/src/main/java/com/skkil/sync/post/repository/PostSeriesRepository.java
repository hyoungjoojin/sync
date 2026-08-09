package com.skkil.sync.post.repository;

import com.skkil.sync.post.model.PostSeries;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostSeriesRepository extends JpaRepository<PostSeries, Long> {

  Optional<PostSeries> findByExternalId(String externalId);

  boolean existsByExternalId(String externalId);

  List<PostSeries> findByCreatorId(Long creatorId);

  List<PostSeries> findByProjectId(Long projectId);

  List<PostSeries> findByProjectIdAndCreatorId(Long projectId, Long creatorId);
}
