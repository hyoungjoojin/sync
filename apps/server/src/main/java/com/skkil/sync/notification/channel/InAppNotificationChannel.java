package com.skkil.sync.notification.channel;

import com.skkil.sync.notification.constant.ChannelType;
import com.skkil.sync.notification.dto.data.NotificationSummary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.websocket.enabled", havingValue = "true", matchIfMissing = false)
public class InAppNotificationChannel implements NotificationChannel {

  private final SimpMessagingTemplate messagingTemplate;

  public InAppNotificationChannel(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public ChannelType type() {
    return ChannelType.IN_APP;
  }

  @Override
  public void send(Long to, NotificationSummary notification) {
    messagingTemplate.convertAndSend("/topic/notifications/" + to, notification);
  }
}
