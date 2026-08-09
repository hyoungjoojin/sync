package com.skkil.sync.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectJoinRequestApprovedEvent extends ApplicationEvent {

  private final Long projectId;
  private final Long requesterId;
  private final Long approverId;

  public ProjectJoinRequestApprovedEvent(Long projectId, Long requesterId, Long approverId) {
    super(projectId);

    this.projectId = projectId;
    this.requesterId = requesterId;
    this.approverId = approverId;
  }
}
