package com.skkil.sync.project.service;

import com.skkil.sync.project.dto.response.GetMyProjectJoinRequestsResponse;
import com.skkil.sync.project.dto.response.GetProjectJoinRequestsResponse;
import com.skkil.sync.project.event.ProjectJoinRequestApprovedEvent;
import com.skkil.sync.project.event.ProjectJoinRequestDeclinedEvent;
import com.skkil.sync.project.event.ProjectJoinRequestedEvent;
import com.skkil.sync.project.exception.ProjectAlreadyTeammateException;
import com.skkil.sync.project.exception.ProjectJoinNotAllowedException;
import com.skkil.sync.project.exception.ProjectJoinRequestAlreadyExistsException;
import com.skkil.sync.project.exception.ProjectJoinRequestNotFoundException;
import com.skkil.sync.project.exception.ProjectNotFoundException;
import com.skkil.sync.project.mapper.ProjectAssembler;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.ProjectJoinRequest;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.ProjectJoinRequestRepository;
import com.skkil.sync.project.repository.ProjectRepository;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectJoinService {

  private final ProjectRepository projectRepository;

  private final TeammateRepository teammateRepository;

  private final ProjectJoinRequestRepository projectJoinRequestRepository;

  private final UserDomainService userDomainService;

  private final ProjectAssembler projectAssembler;

  private final ApplicationEventPublisher eventPublisher;

  public ProjectJoinService(
      ProjectRepository projectRepository,
      TeammateRepository teammateRepository,
      ProjectJoinRequestRepository projectJoinRequestRepository,
      UserDomainService userDomainService,
      ProjectAssembler projectAssembler,
      ApplicationEventPublisher eventPublisher) {
    this.projectRepository = projectRepository;
    this.teammateRepository = teammateRepository;
    this.projectJoinRequestRepository = projectJoinRequestRepository;
    this.userDomainService = userDomainService;
    this.projectAssembler = projectAssembler;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  @PreAuthorize("hasPermission(#projectHandle, 'PROJECT', 'READ')")
  public void joinProject(Long userId, String projectHandle) {
    Project project =
        projectRepository.findByHandle(projectHandle).orElseThrow(ProjectNotFoundException::new);

    if (teammateRepository.findByProjectIdAndUserId(project.getId(), userId).isPresent()) {
      throw new ProjectAlreadyTeammateException();
    }

    User user = userDomainService.getUserReference(userId);

    switch (project.getJoinPolicy()) {
      case OPEN -> {
        Teammate teammate = Teammate.member(project, user);
        project.addTeammate(teammate);
      }
      case REQUEST -> {
        if (projectJoinRequestRepository.existsByProjectIdAndRequesterId(project.getId(), userId)) {
          throw new ProjectJoinRequestAlreadyExistsException();
        }

        ProjectJoinRequest joinRequest =
            projectJoinRequestRepository.save(
                ProjectJoinRequest.builder().project(project).requester(user).build());

        eventPublisher.publishEvent(
            new ProjectJoinRequestedEvent(joinRequest.getId(), project.getId(), userId));
      }
      case INVITE -> throw new ProjectJoinNotAllowedException();
    }
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasPermission(#projectHandle, 'PROJECT', 'EDIT')")
  public GetProjectJoinRequestsResponse getJoinRequests(String projectHandle) {
    Project project =
        projectRepository.findByHandle(projectHandle).orElseThrow(ProjectNotFoundException::new);

    var joinRequests = projectJoinRequestRepository.findByProjectId(project.getId());

    return projectAssembler.toGetProjectJoinRequestsResponse(joinRequests);
  }

  @Transactional(readOnly = true)
  public GetMyProjectJoinRequestsResponse getMyJoinRequests(Long userId) {
    var joinRequests = projectJoinRequestRepository.findByRequesterId(userId);

    return projectAssembler.toGetMyProjectJoinRequestsResponse(joinRequests);
  }

  @Transactional
  public void cancelJoinRequest(Long userId, Long requestId) {
    ProjectJoinRequest joinRequest =
        projectJoinRequestRepository
            .findById(requestId)
            .filter(request -> request.getRequester().getId().equals(userId))
            .orElseThrow(ProjectJoinRequestNotFoundException::new);

    projectJoinRequestRepository.delete(joinRequest);
  }

  @Transactional
  @PreAuthorize("hasPermission(#projectHandle, 'PROJECT', 'EDIT')")
  public void approveJoinRequest(Long userId, String projectHandle, Long requestId) {
    Project project =
        projectRepository.findByHandle(projectHandle).orElseThrow(ProjectNotFoundException::new);

    ProjectJoinRequest joinRequest = getJoinRequest(project, requestId);
    User requester = joinRequest.getRequester();

    projectJoinRequestRepository.delete(joinRequest);

    if (teammateRepository.findByProjectIdAndUserId(project.getId(), requester.getId()).isEmpty()) {
      Teammate teammate = Teammate.member(project, requester);
      project.addTeammate(teammate);
    }

    eventPublisher.publishEvent(
        new ProjectJoinRequestApprovedEvent(project.getId(), requester.getId(), userId));
  }

  @Transactional
  @PreAuthorize("hasPermission(#projectHandle, 'PROJECT', 'EDIT')")
  public void declineJoinRequest(String projectHandle, Long requestId) {
    Project project =
        projectRepository.findByHandle(projectHandle).orElseThrow(ProjectNotFoundException::new);

    ProjectJoinRequest joinRequest = getJoinRequest(project, requestId);
    Long requesterId = joinRequest.getRequester().getId();

    projectJoinRequestRepository.delete(joinRequest);

    eventPublisher.publishEvent(new ProjectJoinRequestDeclinedEvent(project.getId(), requesterId));
  }

  private ProjectJoinRequest getJoinRequest(Project project, Long requestId) {
    return projectJoinRequestRepository
        .findById(requestId)
        .filter(request -> request.getProject().getId().equals(project.getId()))
        .orElseThrow(ProjectJoinRequestNotFoundException::new);
  }
}
