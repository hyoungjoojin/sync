package com.skkil.sync.media.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class UnsupportedMediaTypeException extends SyncException {

  public UnsupportedMediaTypeException(String mediaType) {
    super(String.format("Media type %s is not supported.", mediaType));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
  }
}
