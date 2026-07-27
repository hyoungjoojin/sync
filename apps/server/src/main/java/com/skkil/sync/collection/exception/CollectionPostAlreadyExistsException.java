package com.skkil.sync.collection.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CollectionPostAlreadyExistsException extends SyncException {

  public CollectionPostAlreadyExistsException(Long collectionId, Long postId) {
    super(String.format("Post %d is already in collection %d.", postId, collectionId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.COLLECTION_POST_ALREADY_EXISTS;
  }
}
