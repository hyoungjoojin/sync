package com.skkil.sync.project.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectJoinRequestedEvent extends ApplicationEvent {

  private final Long joinRequestId;
  private final Long projectId;
  private final Long requesterId;

  public ProjectJoinRequestedEvent(Long joinRequestId, Long projectId, Long requesterId) {
    super(joinRequestId);

    this.joinRequestId = joinRequestId;
    this.projectId = projectId;
    this.requesterId = requesterId;
  }
}
