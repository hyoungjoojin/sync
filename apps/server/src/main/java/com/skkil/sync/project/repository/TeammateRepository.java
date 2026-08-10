package com.skkil.sync.project.repository;

import com.skkil.sync.project.model.Teammate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeammateRepository extends JpaRepository<Teammate, Long> {

  @EntityGraph(attributePaths = {"user"})
  List<Teammate> findByProjectId(Long projectId);

  @EntityGraph(attributePaths = {"user"})
  List<Teammate> findByProjectId(Long projectId, Pageable pageable);

  @Query(
      """
      SELECT t FROM Teammate t
      JOIN FETCH t.user
      WHERE t.project.id = :projectId AND (t.isOwner = true OR t.role = com.skkil.sync.project.model.Role.ADMIN)
      """)
  List<Teammate> findManagersByProjectId(Long projectId);

  boolean existsByProjectIdAndUserIdAndIsOwnerTrue(Long projectId, Long userId);

  Optional<Teammate> findByProjectIdAndUserId(Long projectId, Long userId);

  Optional<Teammate> findByProjectHandleAndUserId(String projectHandle, Long userId);

  @EntityGraph(attributePaths = {"project"})
  List<Teammate> findByUserIdAndProjectHandleIn(Long userId, Collection<String> projectHandles);

  @EntityGraph(attributePaths = {"user"})
  Optional<Teammate> findByProjectIdAndUserHandle(Long projectId, String userHandle);

  void deleteByProjectIdAndUserHandle(Long projectId, String userHandle);

  long countByProjectId(Long projectId);

  @Query(
      """
      SELECT t FROM Teammate t
      JOIN FETCH t.user
      WHERE t.project.id IN :projectIds AND t.isOwner = true
      """)
  List<Teammate> findOwnersByProjectIds(Collection<Long> projectIds);
}
