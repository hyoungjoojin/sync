package com.skkil.sync.common.integration.captcha.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CaptchaVerificationFailedException extends SyncException {

  public CaptchaVerificationFailedException() {
    super("Captcha verification failed.");
  }

  public CaptchaVerificationFailedException(Throwable cause) {
    super("Captcha verification failed.", cause);
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.CAPTCHA_VERIFICATION_FAILED;
  }
}
