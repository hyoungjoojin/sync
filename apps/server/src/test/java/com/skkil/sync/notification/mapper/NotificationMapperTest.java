package com.skkil.sync.notification.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.model.NewCommentPayload;
import com.skkil.sync.notification.model.Notification;
import com.skkil.sync.notification.model.WelcomePayload;
import com.skkil.sync.user.mapper.UserMapper;
import com.skkil.sync.user.mapper.UserMapperImpl;
import com.skkil.sync.user.model.User;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationMapperTest {

  private final UserMapper userMapper = new UserMapperImpl();
  private final NotificationMapper notificationMapper = new NotificationMapper(userMapper);

  @Test
  void toPayloadJson_embedsTypeDiscriminator_andRoundTripsThroughToDto() {
    User user = new User(1L);
    NewCommentPayload payload =
        new NewCommentPayload("actor-handle", "Actor Name", "Post Title", "post-slug");

    var payloadJson = notificationMapper.toPayloadJson(payload);
    assertThat(payloadJson.get("type").asText()).isEqualTo("NEW_COMMENT");

    Notification notification =
        Notification.builder()
            .user(user)
            .type(NotificationType.NEW_COMMENT)
            .payload(payloadJson)
            .build();

    var dto = notificationMapper.toDto(notification, Map.of());

    assertThat(dto.payload()).isEqualTo(payload);
  }

  @Test
  void toPayloadJson_roundTripsPayloadWithNoFields() {
    User user = new User(1L);
    WelcomePayload payload = new WelcomePayload();

    var payloadJson = notificationMapper.toPayloadJson(payload);
    assertThat(payloadJson.get("type").asText()).isEqualTo("WELCOME");

    Notification notification =
        Notification.builder()
            .user(user)
            .type(NotificationType.WELCOME)
            .payload(payloadJson)
            .build();

    var dto = notificationMapper.toDto(notification, Map.of());

    assertThat(dto.payload()).isEqualTo(payload);
  }
}
