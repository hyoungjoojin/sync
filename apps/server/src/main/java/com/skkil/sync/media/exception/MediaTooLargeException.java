package com.skkil.sync.media.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class MediaTooLargeException extends SyncException {

  public MediaTooLargeException(long fileSize, long maxFileSize) {
    super(
        String.format(
            "Media size %d bytes exceeds the maximum of %d bytes.", fileSize, maxFileSize));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.MEDIA_TOO_LARGE;
  }
}
