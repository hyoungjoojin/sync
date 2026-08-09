package com.skkil.sync.notification.model;

public record ProjectJoinRequestApprovedPayload(
    String actorHandle, String actorName, String projectHandle, String projectName)
    implements NotificationPayload {}
