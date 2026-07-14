package com.skkil.sync.notification.listener;

import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.event.NotificationEvent;
import com.skkil.sync.notification.model.ProjectInvitationPayload;
import com.skkil.sync.project.event.ProjectInvitationCreatedEvent;
import com.skkil.sync.project.model.Project;
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
public class ProjectInvitationNotificationListener {

  private final ApplicationEventPublisher eventPublisher;
  private final UserDomainService userDomainService;
  private final ProjectDomainService projectDomainService;

  public ProjectInvitationNotificationListener(
      ApplicationEventPublisher eventPublisher,
      UserDomainService userDomainService,
      ProjectDomainService projectDomainService) {
    this.eventPublisher = eventPublisher;
    this.userDomainService = userDomainService;
    this.projectDomainService = projectDomainService;
  }

  @Async
  @TransactionalEventListener
  public void handleProjectInvitationCreatedEvent(ProjectInvitationCreatedEvent event) {
    log.debug(
        "Project invitation created event received for invitation ID: {}", event.getInvitationId());

    User inviter = userDomainService.getUser(event.getInviterId());
    Project project = projectDomainService.getProject(event.getProjectId());

    eventPublisher.publishEvent(
        new NotificationEvent(
            event.getInviteeId(),
            NotificationType.PROJECT_INVITATION,
            event.getInviterId(),
            NotificationEntityType.PROJECT_INVITATION,
            event.getInvitationId(),
            new ProjectInvitationPayload(
                inviter.getHandle(),
                inviter.getFullName(),
                project.getHandle(),
                project.getName())));
  }
}
