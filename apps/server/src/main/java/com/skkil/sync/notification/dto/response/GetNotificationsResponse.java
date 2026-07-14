package com.skkil.sync.notification.dto.response;

import com.skkil.sync.notification.dto.data.NotificationSummary;
import org.springframework.data.domain.Page;

public record GetNotificationsResponse(Page<NotificationSummary> notifications, long unreadCount) {}
