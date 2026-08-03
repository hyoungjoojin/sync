package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import com.skkil.sync.post.constants.PostConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostPinLimitExceededException extends SyncException {

  public PostPinLimitExceededException() {
    super(
        String.format(
            "A project cannot have more than %d pinned posts",
            PostConstants.MAX_PINNED_POSTS_PER_PROJECT));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_PIN_LIMIT_EXCEEDED;
  }
}
