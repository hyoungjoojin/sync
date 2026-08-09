package com.skkil.sync.notification.dto.response;

import com.skkil.sync.common.util.pagination.dto.response.OffsetPaginationResponse;
import com.skkil.sync.notification.dto.data.NotificationSummary;

public record GetNotificationsResponse(
    OffsetPaginationResponse<NotificationSummary> notifications, long unreadCount) {}
