package com.skkil.sync.notification.service;

import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.dto.response.GetNotificationsResponse;
import com.skkil.sync.notification.exception.NotificationNotFoundException;
import com.skkil.sync.notification.mapper.NotificationMapper;
import com.skkil.sync.notification.model.Notification;
import com.skkil.sync.notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final MediaDomainService mediaDomainService;

  public NotificationService(
      NotificationRepository notificationRepository,
      NotificationMapper notificationMapper,
      MediaDomainService mediaDomainService) {
    this.notificationRepository = notificationRepository;
    this.notificationMapper = notificationMapper;
    this.mediaDomainService = mediaDomainService;
  }

  @Transactional(readOnly = true)
  public GetNotificationsResponse getNotifications(Long userId, int size, Long cursor) {
    Pageable pageable = Pageable.ofSize(size);

    var notifications = notificationRepository.findByUser(userId, pageable, cursor);

    var actorProfileImageUrls =
        mediaDomainService.generatePublicGetUrls(
            notifications.getContent(),
            notification -> {
              var actor = notification.getActor();
              return actor == null ? null : actor.getProfileImage();
            });

    var notificationDtos =
        notifications.map(
            notification -> notificationMapper.toDto(notification, actorProfileImageUrls));

    long unreadCount =
        notificationRepository.countByUser_IdAndStatus(userId, NotificationStatus.UNREAD);

    return new GetNotificationsResponse(notificationDtos, unreadCount);
  }

  @Transactional
  public void markAsRead(Long userId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findByIdAndUser_Id(notificationId, userId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    notification.markAsRead();
  }

  @Transactional
  public void markAllAsRead(Long userId) {
    notificationRepository.markAllAsRead(
        userId, NotificationStatus.UNREAD, NotificationStatus.READ);
  }
}
