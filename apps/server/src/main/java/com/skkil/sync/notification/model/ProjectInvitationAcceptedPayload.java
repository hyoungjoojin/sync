package com.skkil.sync.notification.model;

public record ProjectInvitationAcceptedPayload(
    String actorHandle, String actorName, String projectHandle, String projectName)
    implements NotificationPayload {}
