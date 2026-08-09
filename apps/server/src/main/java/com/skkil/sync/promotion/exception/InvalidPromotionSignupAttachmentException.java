package com.skkil.sync.promotion.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class InvalidPromotionSignupAttachmentException extends SyncException {

  public InvalidPromotionSignupAttachmentException(String message) {
    super(message);
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROMOTION_SIGNUP_ATTACHMENT_INVALID;
  }
}
