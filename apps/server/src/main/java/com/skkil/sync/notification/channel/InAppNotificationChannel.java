package com.skkil.sync.notification.channel;

import com.skkil.sync.notification.constant.ChannelType;
import com.skkil.sync.notification.dto.data.NotificationSummary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
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
