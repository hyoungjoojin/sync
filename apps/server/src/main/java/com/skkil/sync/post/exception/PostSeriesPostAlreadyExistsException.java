package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostSeriesPostAlreadyExistsException extends SyncException {

  public PostSeriesPostAlreadyExistsException(Long seriesId, Long postId) {
    super(String.format("Post %d is already in series %d.", postId, seriesId));
  }

  public PostSeriesPostAlreadyExistsException(Long postId) {
    super(String.format("Post %d already belongs to another series.", postId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_SERIES_POST_ALREADY_EXISTS;
  }
}
