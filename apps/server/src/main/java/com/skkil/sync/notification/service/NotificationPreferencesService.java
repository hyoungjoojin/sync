package com.skkil.sync.notification.service;

import com.skkil.sync.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.skkil.sync.notification.dto.response.GetNotificationPreferencesResponse;
import com.skkil.sync.notification.model.NotificationPreference;
import com.skkil.sync.notification.repository.NotificationPreferenceRepository;
import com.skkil.sync.user.service.domain.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferencesService {

  private final NotificationPreferenceRepository notificationPreferenceRepository;
  private final UserDomainService userDomainService;

  public NotificationPreferencesService(
      NotificationPreferenceRepository notificationPreferenceRepository,
      UserDomainService userDomainService) {
    this.notificationPreferenceRepository = notificationPreferenceRepository;
    this.userDomainService = userDomainService;
  }

  @Transactional(readOnly = true)
  public boolean isInAppEnabled(Long userId) {
    return notificationPreferenceRepository
        .findByUserId(userId)
        .map(NotificationPreference::isInAppEnabled)
        .orElse(true);
  }

  @Transactional(readOnly = true)
  public GetNotificationPreferencesResponse getPreferences(Long userId) {
    return new GetNotificationPreferencesResponse(isInAppEnabled(userId));
  }

  @Transactional
  public void updatePreferences(Long userId, UpdateNotificationPreferencesRequest request) {
    NotificationPreference preference =
        notificationPreferenceRepository
            .findByUserId(userId)
            .orElseGet(
                () -> {
                  NotificationPreference newPreference = new NotificationPreference();
                  newPreference.setUser(userDomainService.getUserReference(userId));
                  return newPreference;
                });

    preference.updateInAppEnabled(request.inAppEnabled());
    notificationPreferenceRepository.save(preference);
  }
}
