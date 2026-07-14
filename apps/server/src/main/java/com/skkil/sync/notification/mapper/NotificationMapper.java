package com.skkil.sync.notification.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skkil.sync.notification.dto.data.NotificationSummary;
import com.skkil.sync.notification.model.Notification;
import com.skkil.sync.notification.model.NotificationPayload;
import com.skkil.sync.user.dto.summary.UserSummary;
import com.skkil.sync.user.mapper.UserMapper;
import com.skkil.sync.user.model.User;
import java.net.URL;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UserMapper userMapper;

  public NotificationMapper(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public JsonNode toPayloadJson(NotificationPayload payload) {
    return objectMapper.valueToTree(payload);
  }

  public NotificationSummary toDto(
      Notification notification, Map<Long, URL> actorProfileImageUrls) {
    return new NotificationSummary(
        notification.getId(),
        notification.getType(),
        notification.getStatus(),
        notification.getCreatedAt(),
        toUserSummary(notification.getActor(), actorProfileImageUrls),
        notification.getEntityType(),
        notification.getEntityId(),
        toPayload(notification.getPayload()));
  }

  private @Nullable UserSummary toUserSummary(
      @Nullable User actor, Map<Long, URL> actorProfileImageUrls) {
    if (actor == null) {
      return null;
    }

    URL profileImageUrl =
        actor.getProfileImage() == null
            ? null
            : actorProfileImageUrls.get(actor.getProfileImage().getId());

    return userMapper.toUserSummary(
        actor, profileImageUrl == null ? null : profileImageUrl.toString());
  }

  private NotificationPayload toPayload(JsonNode json) {
    return objectMapper.convertValue(json, NotificationPayload.class);
  }
}
