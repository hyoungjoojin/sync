package com.skkil.sync.project.dto.response;

import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import java.util.List;

public record GetProjectJoinRequestsResponse(List<JoinRequest> joinRequests) {

  public record JoinRequest(Long id, UserSummary requester, Instant createdAt) {}
}
