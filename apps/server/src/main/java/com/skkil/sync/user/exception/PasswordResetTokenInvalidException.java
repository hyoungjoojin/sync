package com.skkil.sync.user.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PasswordResetTokenInvalidException extends SyncException {

  public PasswordResetTokenInvalidException() {
    super("Password reset token is invalid.");
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.INVALID_PASSWORD_RESET_TOKEN;
  }
}
