package com.skkil.sync.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectInvitationCreatedEvent extends ApplicationEvent {

  private final Long invitationId;
  private final Long projectId;
  private final Long inviterId;
  private final Long inviteeId;
  private final String token;

  public ProjectInvitationCreatedEvent(
      Long invitationId, Long projectId, Long inviterId, Long inviteeId, String token) {
    super(invitationId);

    this.invitationId = invitationId;
    this.projectId = projectId;
    this.inviterId = inviterId;
    this.inviteeId = inviteeId;
    this.token = token;
  }
}
