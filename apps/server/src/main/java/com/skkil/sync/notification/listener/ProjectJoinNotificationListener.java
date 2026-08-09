package com.skkil.sync.notification.listener;

import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.event.NotificationEvent;
import com.skkil.sync.notification.model.ProjectJoinRequestApprovedPayload;
import com.skkil.sync.notification.model.ProjectJoinRequestDeclinedPayload;
import com.skkil.sync.notification.model.ProjectJoinRequestPayload;
import com.skkil.sync.project.event.ProjectJoinRequestApprovedEvent;
import com.skkil.sync.project.event.ProjectJoinRequestDeclinedEvent;
import com.skkil.sync.project.event.ProjectJoinRequestedEvent;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.project.service.ProjectDomainService;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class ProjectJoinNotificationListener {

  private final ApplicationEventPublisher eventPublisher;
  private final UserDomainService userDomainService;
  private final ProjectDomainService projectDomainService;
  private final TeammateRepository teammateRepository;

  public ProjectJoinNotificationListener(
      ApplicationEventPublisher eventPublisher,
      UserDomainService userDomainService,
      ProjectDomainService projectDomainService,
      TeammateRepository teammateRepository) {
    this.eventPublisher = eventPublisher;
    this.userDomainService = userDomainService;
    this.projectDomainService = projectDomainService;
    this.teammateRepository = teammateRepository;
  }

  /**
   * A join request is addressed to whoever can act on it, so every manager of the project is
   * notified rather than a single owner — an unattended request is the whole defect this closes.
   */
  @Async
  @TransactionalEventListener
  public void handleProjectJoinRequestedEvent(ProjectJoinRequestedEvent event) {
    log.debug(
        "Project join requested event received for join request ID: {}", event.getJoinRequestId());

    User requester = userDomainService.getUser(event.getRequesterId());
    Project project = projectDomainService.getProject(event.getProjectId());

    ProjectJoinRequestPayload payload =
        new ProjectJoinRequestPayload(
            requester.getHandle(), requester.getFullName(), project.getHandle(), project.getName());

    for (Teammate manager : teammateRepository.findManagersByProjectId(event.getProjectId())) {
      eventPublisher.publishEvent(
          new NotificationEvent(
              manager.getUser().getId(),
              NotificationType.PROJECT_JOIN_REQUEST,
              event.getRequesterId(),
              NotificationEntityType.PROJECT_JOIN_REQUEST,
              event.getJoinRequestId(),
              payload));
    }
  }

  @Async
  @TransactionalEventListener
  public void handleProjectJoinRequestApprovedEvent(ProjectJoinRequestApprovedEvent event) {
    log.debug(
        "Project join request approved event received for project ID: {}", event.getProjectId());

    User approver = userDomainService.getUser(event.getApproverId());
    Project project = projectDomainService.getProject(event.getProjectId());

    eventPublisher.publishEvent(
        new NotificationEvent(
            event.getRequesterId(),
            NotificationType.PROJECT_JOIN_REQUEST_APPROVED,
            event.getApproverId(),
            NotificationEntityType.PROJECT,
            event.getProjectId(),
            new ProjectJoinRequestApprovedPayload(
                approver.getHandle(),
                approver.getFullName(),
                project.getHandle(),
                project.getName())));
  }

  /**
   * A decline carries no actor: naming the manager who rejected a request exposes them personally
   * for a moderation decision that belongs to the project.
   */
  @Async
  @TransactionalEventListener
  public void handleProjectJoinRequestDeclinedEvent(ProjectJoinRequestDeclinedEvent event) {
    log.debug(
        "Project join request declined event received for project ID: {}", event.getProjectId());

    Project project = projectDomainService.getProject(event.getProjectId());

    eventPublisher.publishEvent(
        new NotificationEvent(
            event.getRequesterId(),
            NotificationType.PROJECT_JOIN_REQUEST_DECLINED,
            null,
            NotificationEntityType.PROJECT,
            event.getProjectId(),
            new ProjectJoinRequestDeclinedPayload(project.getHandle(), project.getName())));
  }
}
