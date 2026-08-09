package com.skkil.sync.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectJoinRequestDeclinedEvent extends ApplicationEvent {

  private final Long projectId;
  private final Long requesterId;

  public ProjectJoinRequestDeclinedEvent(Long projectId, Long requesterId) {
    super(projectId);

    this.projectId = projectId;
    this.requesterId = requesterId;
  }
}
