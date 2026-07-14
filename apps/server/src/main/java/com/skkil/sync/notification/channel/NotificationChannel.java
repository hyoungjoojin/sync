package com.skkil.sync.notification.channel;

import com.skkil.sync.notification.constant.ChannelType;
import com.skkil.sync.notification.dto.data.NotificationSummary;

public interface NotificationChannel {

  ChannelType type();

  void send(Long recipientId, NotificationSummary notification);
}
