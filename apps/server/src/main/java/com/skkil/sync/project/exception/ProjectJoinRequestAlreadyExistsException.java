package com.skkil.sync.project.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ProjectJoinRequestAlreadyExistsException extends SyncException {

  public ProjectJoinRequestAlreadyExistsException() {
    super("A pending join request already exists for this project.");
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROJECT_JOIN_REQUEST_ALREADY_EXISTS;
  }
}
