package com.skkil.sync.notification.model;

/**
 * Carries the invitation token so the invitee can accept or decline straight from the notification
 * — {@code /invitations/{token}/accept} is keyed by token, not by invitation ID. The token is no
 * more exposed here than in {@code GET /invitations}: both are readable only by the invitee.
 */
public record ProjectInvitationPayload(
    String actorHandle, String actorName, String projectHandle, String projectName, String token)
    implements NotificationPayload {}
