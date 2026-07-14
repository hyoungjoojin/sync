package com.skkil.sync.notification.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = WelcomePayload.class, name = "WELCOME"),
  @JsonSubTypes.Type(value = NewCommentPayload.class, name = "NEW_COMMENT"),
  @JsonSubTypes.Type(value = NewFollowerPayload.class, name = "NEW_FOLLOWER"),
  @JsonSubTypes.Type(value = ProjectInvitationPayload.class, name = "PROJECT_INVITATION"),
  @JsonSubTypes.Type(value = NewMessagePayload.class, name = "NEW_MESSAGE"),
})
public sealed interface NotificationPayload
    permits WelcomePayload,
        NewCommentPayload,
        NewFollowerPayload,
        ProjectInvitationPayload,
        NewMessagePayload {}
