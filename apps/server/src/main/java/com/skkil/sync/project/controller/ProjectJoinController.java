package com.skkil.sync.project.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.project.dto.response.GetMyProjectJoinRequestsResponse;
import com.skkil.sync.project.dto.response.GetProjectJoinRequestsResponse;
import com.skkil.sync.project.service.ProjectJoinService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ProjectJoinController {

  private final ProjectJoinService projectJoinService;

  public ProjectJoinController(ProjectJoinService projectJoinService) {
    this.projectJoinService = projectJoinService;
  }

  @PostMapping("/projects/{handle}/join")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void joinProject(
      @AuthenticationPrincipal @NotNull AuthenticatedUser user, @PathVariable String handle) {
    projectJoinService.joinProject(user.userId(), handle);
  }

  @GetMapping("/projects/{handle}/join-requests")
  @ResponseStatus(HttpStatus.OK)
  public GetProjectJoinRequestsResponse getJoinRequests(@PathVariable String handle) {
    return projectJoinService.getJoinRequests(handle);
  }

  @GetMapping("/join-requests")
  @ResponseStatus(HttpStatus.OK)
  public GetMyProjectJoinRequestsResponse getMyJoinRequests(
      @AuthenticationPrincipal @NotNull AuthenticatedUser user) {
    return projectJoinService.getMyJoinRequests(user.userId());
  }

  @DeleteMapping("/join-requests/{requestId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelJoinRequest(
      @AuthenticationPrincipal @NotNull AuthenticatedUser user, @PathVariable Long requestId) {
    projectJoinService.cancelJoinRequest(user.userId(), requestId);
  }

  @PostMapping("/projects/{handle}/join-requests/{requestId}/approve")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approveJoinRequest(@PathVariable String handle, @PathVariable Long requestId) {
    projectJoinService.approveJoinRequest(handle, requestId);
  }

  @PostMapping("/projects/{handle}/join-requests/{requestId}/decline")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void declineJoinRequest(@PathVariable String handle, @PathVariable Long requestId) {
    projectJoinService.declineJoinRequest(handle, requestId);
  }
}
