package com.skkil.sync.notification.model;

public record NewCommentPayload(
    String actorHandle, String actorName, String postTitle, String postSlug)
    implements NotificationPayload {}
