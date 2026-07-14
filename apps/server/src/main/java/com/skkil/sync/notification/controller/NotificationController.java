package com.skkil.sync.notification.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.util.pagination.dto.request.OffsetPaginationRequest;
import com.skkil.sync.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.skkil.sync.notification.dto.response.GetNotificationPreferencesResponse;
import com.skkil.sync.notification.dto.response.GetNotificationsResponse;
import com.skkil.sync.notification.service.NotificationPreferencesService;
import com.skkil.sync.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationPreferencesService notificationPreferencesService;

  public NotificationController(
      NotificationService notificationService,
      NotificationPreferencesService notificationPreferencesService) {
    this.notificationService = notificationService;
    this.notificationPreferencesService = notificationPreferencesService;
  }

  @GetMapping("/notifications")
  public GetNotificationsResponse getNotifications(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Validated OffsetPaginationRequest pagination) {
    return notificationService.getNotifications(user.userId(), pagination);
  }

  @PatchMapping("/notifications/{notificationId}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsRead(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long notificationId) {
    notificationService.markAsRead(user.userId(), notificationId);
  }

  @PatchMapping("/notifications/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllAsRead(@AuthenticationPrincipal AuthenticatedUser user) {
    notificationService.markAllAsRead(user.userId());
  }

  @GetMapping("/notifications/preferences")
  public GetNotificationPreferencesResponse getPreferences(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return notificationPreferencesService.getPreferences(user.userId());
  }

  @PatchMapping("/notifications/preferences")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePreferences(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestBody UpdateNotificationPreferencesRequest request) {
    notificationPreferencesService.updatePreferences(user.userId(), request);
  }
}
