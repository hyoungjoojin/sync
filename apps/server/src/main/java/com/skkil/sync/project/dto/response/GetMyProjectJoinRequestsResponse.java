package com.skkil.sync.project.dto.response;

import com.skkil.sync.project.dto.summary.ProjectSummary;
import java.time.Instant;
import java.util.List;

public record GetMyProjectJoinRequestsResponse(List<JoinRequest> joinRequests) {

  public record JoinRequest(Long id, ProjectSummary project, Instant createdAt) {}
}
