package com.skkil.sync.media.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class MediaNotUploadedException extends SyncException {

  public MediaNotUploadedException(Long mediaId, Throwable cause) {
    super(String.format("Media with id %d has not been uploaded.", mediaId), cause);
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.MEDIA_NOT_UPLOADED;
  }
}
