package com.skkil.sync.notification.model;

public record NewMessagePayload(
    String actorHandle, String actorName, Long conversationId, String messagePreview)
    implements NotificationPayload {}
