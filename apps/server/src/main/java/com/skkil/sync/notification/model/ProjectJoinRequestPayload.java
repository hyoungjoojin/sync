package com.skkil.sync.notification.model;

public record ProjectJoinRequestPayload(
    String actorHandle, String actorName, String projectHandle, String projectName)
    implements NotificationPayload {}
