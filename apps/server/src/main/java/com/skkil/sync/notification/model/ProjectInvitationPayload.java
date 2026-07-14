package com.skkil.sync.notification.model;

public record ProjectInvitationPayload(
    String actorHandle, String actorName, String projectHandle, String projectName)
    implements NotificationPayload {}
