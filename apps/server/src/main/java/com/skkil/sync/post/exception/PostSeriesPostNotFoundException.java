package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostSeriesPostNotFoundException extends SyncException {

  public PostSeriesPostNotFoundException(Long seriesId, Long seriesPostId) {
    super(String.format("PostSeries post %d is not in series %d.", seriesPostId, seriesId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_SERIES_POST_NOT_FOUND;
  }
}
