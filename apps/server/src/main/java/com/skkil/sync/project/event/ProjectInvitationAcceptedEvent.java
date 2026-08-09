package com.skkil.sync.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectInvitationAcceptedEvent extends ApplicationEvent {

  private final Long invitationId;
  private final Long projectId;
  private final Long inviterId;
  private final Long inviteeId;

  public ProjectInvitationAcceptedEvent(
      Long invitationId, Long projectId, Long inviterId, Long inviteeId) {
    super(invitationId);

    this.invitationId = invitationId;
    this.projectId = projectId;
    this.inviterId = inviterId;
    this.inviteeId = inviteeId;
  }
}
