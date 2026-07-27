package com.skkil.sync.project.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ProjectJoinNotAllowedException extends SyncException {

  public ProjectJoinNotAllowedException() {
    super("This project cannot be joined with its current join policy.");
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROJECT_JOIN_NOT_ALLOWED;
  }
}
