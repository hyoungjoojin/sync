package com.skkil.sync.notification.model;

public record ProjectInvitationDeclinedPayload(
    String actorHandle, String actorName, String projectHandle, String projectName)
    implements NotificationPayload {}
