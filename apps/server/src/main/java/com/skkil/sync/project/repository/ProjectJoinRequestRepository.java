package com.skkil.sync.project.repository;

import com.skkil.sync.project.model.ProjectJoinRequest;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJoinRequestRepository extends JpaRepository<ProjectJoinRequest, Long> {

  @EntityGraph(attributePaths = {"requester"})
  List<ProjectJoinRequest> findByProjectId(Long projectId);

  @EntityGraph(attributePaths = {"project"})
  List<ProjectJoinRequest> findByRequesterId(Long requesterId);

  boolean existsByProjectIdAndRequesterId(Long projectId, Long requesterId);
}
