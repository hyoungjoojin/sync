package com.skkil.sync.notification.dto.data;

import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.model.NotificationPayload;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record NotificationSummary(
    Long id,
    NotificationType type,
    NotificationStatus status,
    Instant createdAt,
    @Nullable UserSummary actor,
    @Nullable NotificationEntityType entityType,
    @Nullable Long entityId,
    NotificationPayload payload) {}
