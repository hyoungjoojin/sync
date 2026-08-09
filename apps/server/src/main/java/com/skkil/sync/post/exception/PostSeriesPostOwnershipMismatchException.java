package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostSeriesPostOwnershipMismatchException extends SyncException {

  public PostSeriesPostOwnershipMismatchException(Long seriesId, Long postId) {
    super(String.format("Post %d does not belong to the owner of series %d.", postId, seriesId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_SERIES_POST_OWNERSHIP_MISMATCH;
  }
}
