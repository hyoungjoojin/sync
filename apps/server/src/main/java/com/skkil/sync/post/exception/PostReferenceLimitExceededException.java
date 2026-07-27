package com.skkil.sync.post.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import com.skkil.sync.post.constants.PostConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostReferenceLimitExceededException extends SyncException {

  public PostReferenceLimitExceededException() {
    super(
        String.format(
            "A post cannot reference more than %d posts", PostConstants.MAX_REFERENCES_PER_POST));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.POST_REFERENCE_LIMIT_EXCEEDED;
  }
}
