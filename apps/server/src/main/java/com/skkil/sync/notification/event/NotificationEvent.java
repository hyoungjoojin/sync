package com.skkil.sync.notification.event;

import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.model.NotificationPayload;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {

  private final Long recipientId;
  private final NotificationType notificationType;
  private final @Nullable Long actorId;
  private final @Nullable NotificationEntityType entityType;
  private final @Nullable Long entityId;
  private final NotificationPayload payload;

  public NotificationEvent(
      Long recipientId,
      NotificationType notificationType,
      @Nullable Long actorId,
      @Nullable NotificationEntityType entityType,
      @Nullable Long entityId,
      NotificationPayload payload) {
    super(recipientId);

    if ((entityType == null) != (entityId == null)) {
      throw new IllegalArgumentException("entityType and entityId must be set together");
    }

    this.recipientId = recipientId;
    this.notificationType = notificationType;
    this.actorId = actorId;
    this.entityType = entityType;
    this.entityId = entityId;
    this.payload = Objects.requireNonNull(payload, "payload must not be null");
  }
}
