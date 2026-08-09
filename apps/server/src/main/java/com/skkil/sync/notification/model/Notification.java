package com.skkil.sync.notification.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.skkil.sync.common.domain.BaseEntity;
import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "notifications")
@Getter
public class Notification extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "actor_id")
  private @Nullable User actor;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private NotificationStatus status = NotificationStatus.UNREAD;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", length = 50)
  private @Nullable NotificationEntityType entityType;

  @Column(name = "entity_id")
  private @Nullable Long entityId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
  private JsonNode payload;

  protected Notification() {}

  @Builder
  public Notification(
      User user,
      @Nullable User actor,
      NotificationType type,
      @Nullable NotificationEntityType entityType,
      @Nullable Long entityId,
      JsonNode payload) {
    if ((entityType == null) != (entityId == null)) {
      throw new IllegalArgumentException("entityType and entityId must be set together");
    }

    this.user = user;
    this.actor = actor;
    this.type = type;
    this.entityType = entityType;
    this.entityId = entityId;
    this.payload = payload;
  }

  public void markAsRead() {
    this.status = NotificationStatus.READ;
  }
}
