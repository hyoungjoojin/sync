package com.skkil.sync.project.service;

import com.skkil.sync.project.dto.summary.AdminProjectSummary;
import com.skkil.sync.project.exception.ProjectNotFoundException;
import com.skkil.sync.project.mapper.AdminProjectAssembler;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProjectService {

  private static final Logger log = LoggerFactory.getLogger(AdminProjectService.class);

  private final ProjectRepository projectRepository;

  private final ProjectDeletionService projectDeletionService;

  private final AdminProjectAssembler adminProjectAssembler;

  public AdminProjectService(
      ProjectRepository projectRepository,
      ProjectDeletionService projectDeletionService,
      AdminProjectAssembler adminProjectAssembler) {
    this.projectRepository = projectRepository;
    this.projectDeletionService = projectDeletionService;
    this.adminProjectAssembler = adminProjectAssembler;
  }

  @Transactional(readOnly = true)
  public AdminProjectSummary searchProject(String query) {
    Project project =
        projectRepository.searchProjectForAdmin(query).orElseThrow(ProjectNotFoundException::new);

    return adminProjectAssembler.toAdminProjectSummary(project);
  }

  @Transactional
  public void deleteProject(String handle) {
    Project project =
        projectRepository.findByHandleForUpdate(handle).orElseThrow(ProjectNotFoundException::new);

    projectDeletionService.delete(project);
    log.warn("관리자가 프로젝트를 삭제했습니다. id={}, handle={}", project.getId(), handle);
  }
}
