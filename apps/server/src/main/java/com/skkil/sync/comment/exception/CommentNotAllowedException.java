package com.skkil.sync.comment.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CommentNotAllowedException extends SyncException {

  public CommentNotAllowedException(String slug) {
    super(String.format("Comments on post '%s' are limited to its project members.", slug));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.COMMENT_NOT_ALLOWED;
  }
}
