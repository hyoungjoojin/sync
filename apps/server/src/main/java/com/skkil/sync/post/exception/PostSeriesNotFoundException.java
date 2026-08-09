package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostSeriesNotFoundException extends SyncException {

  public PostSeriesNotFoundException(Long seriesId) {
    super(String.format("PostSeries with id %d not found.", seriesId));
  }

  public PostSeriesNotFoundException(String externalId) {
    super(String.format("PostSeries with external id '%s' not found.", externalId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_SERIES_NOT_FOUND;
  }
}
