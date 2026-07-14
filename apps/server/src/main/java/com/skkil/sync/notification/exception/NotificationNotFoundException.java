package com.skkil.sync.notification.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class NotificationNotFoundException extends SyncException {

  public NotificationNotFoundException(Long notificationId) {
    super(String.format("Notification with id %d not found.", notificationId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.NOTIFICATION_NOT_FOUND;
  }
}
