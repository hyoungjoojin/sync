package com.skkil.sync.notification.listener;

import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.event.NotificationEvent;
import com.skkil.sync.notification.model.NewFollowerPayload;
import com.skkil.sync.user.event.UserFollowedEvent;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class UserFollowedNotificationListener {

  private final ApplicationEventPublisher eventPublisher;
  private final UserDomainService userDomainService;

  public UserFollowedNotificationListener(
      ApplicationEventPublisher eventPublisher, UserDomainService userDomainService) {
    this.eventPublisher = eventPublisher;
    this.userDomainService = userDomainService;
  }

  @Async
  @TransactionalEventListener
  public void handleUserFollowedEvent(UserFollowedEvent event) {
    log.debug(
        "User followed event received: {} followed {}",
        event.getFollowerId(),
        event.getFolloweeId());

    User follower = userDomainService.getUser(event.getFollowerId());

    eventPublisher.publishEvent(
        new NotificationEvent(
            event.getFolloweeId(),
            NotificationType.NEW_FOLLOWER,
            event.getFollowerId(),
            null,
            null,
            new NewFollowerPayload(follower.getHandle(), follower.getFullName())));
  }
}
