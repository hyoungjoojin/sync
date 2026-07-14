package com.skkil.sync.notification.listener;

import com.skkil.sync.comment.event.CommentCreatedEvent;
import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.event.NotificationEvent;
import com.skkil.sync.notification.model.NewCommentPayload;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class CommentCreatedNotificationListener {

  private final ApplicationEventPublisher eventPublisher;
  private final UserDomainService userDomainService;
  private final PostDomainService postDomainService;

  public CommentCreatedNotificationListener(
      ApplicationEventPublisher eventPublisher,
      UserDomainService userDomainService,
      PostDomainService postDomainService) {
    this.eventPublisher = eventPublisher;
    this.userDomainService = userDomainService;
    this.postDomainService = postDomainService;
  }

  @Async
  @TransactionalEventListener
  public void handleCommentCreatedEvent(CommentCreatedEvent event) {
    log.debug("Comment created event received for comment ID: {}", event.getCommentId());

    if (event.getCommenterId().equals(event.getPostAuthorId())) {
      return;
    }

    User commenter = userDomainService.getUser(event.getCommenterId());
    Post post = postDomainService.getPost(event.getPostId());

    eventPublisher.publishEvent(
        new NotificationEvent(
            event.getPostAuthorId(),
            NotificationType.NEW_COMMENT,
            event.getCommenterId(),
            NotificationEntityType.COMMENT,
            event.getCommentId(),
            new NewCommentPayload(
                commenter.getHandle(), commenter.getFullName(), post.getTitle(), post.getSlug())));
  }
}
