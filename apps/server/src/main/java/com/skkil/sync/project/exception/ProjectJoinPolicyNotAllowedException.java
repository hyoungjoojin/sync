package com.skkil.sync.project.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ProjectJoinPolicyNotAllowedException extends SyncException {

  public ProjectJoinPolicyNotAllowedException() {
    super("A private project must use the INVITE join policy.");
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.PROJECT_JOIN_POLICY_NOT_ALLOWED;
  }
}
