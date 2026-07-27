package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import com.skkil.sync.post.constants.PostConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostSeriesPostLimitExceededException extends SyncException {

  public PostSeriesPostLimitExceededException() {
    super(
        String.format(
            "A series cannot have more than %d posts", PostConstants.MAX_POSTS_PER_SERIES));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_SERIES_POST_LIMIT_EXCEEDED;
  }
}
