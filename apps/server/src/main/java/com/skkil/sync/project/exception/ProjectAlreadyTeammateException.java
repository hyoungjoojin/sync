package com.skkil.sync.project.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ProjectAlreadyTeammateException extends SyncException {

  public ProjectAlreadyTeammateException() {
    super("The user is already a teammate of this project.");
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROJECT_ALREADY_TEAMMATE;
  }
}
