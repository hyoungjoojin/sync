package com.skkil.sync.promotion.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PromotionNotFoundException extends SyncException {

  public PromotionNotFoundException(Long promotionId) {
    super(String.format("Promotion %d not found.", promotionId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROMOTION_NOT_FOUND;
  }
}
