package com.skkil.sync.common.integration.channeltalk.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ChannelTalkMemberHashFailedException extends SyncException {

  public ChannelTalkMemberHashFailedException(Throwable cause) {
    super("Failed to generate Channel Talk member hash", cause);
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.CHANNEL_TALK_MEMBER_HASH_FAILED;
  }
}
