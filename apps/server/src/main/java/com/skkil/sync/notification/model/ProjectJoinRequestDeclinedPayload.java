package com.skkil.sync.notification.model;

public record ProjectJoinRequestDeclinedPayload(String projectHandle, String projectName)
    implements NotificationPayload {}
