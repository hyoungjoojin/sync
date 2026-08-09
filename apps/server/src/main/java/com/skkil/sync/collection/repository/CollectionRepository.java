package com.skkil.sync.collection.repository;

import com.skkil.sync.collection.model.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

  Optional<Collection> findByExternalId(String externalId);

  @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.project WHERE c.externalId = :externalId")
  Optional<Collection> findByExternalIdWithProject(String externalId);

  boolean existsByExternalId(String externalId);

  List<Collection> findByCreatorId(Long creatorId);

  List<Collection> findByProjectId(Long projectId);
}
