package com.skkil.sync.notification.model;

public record NewFollowerPayload(String actorHandle, String actorName)
    implements NotificationPayload {}
