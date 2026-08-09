package com.skkil.sync.collection.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CollectionPostNotFoundException extends SyncException {

  public CollectionPostNotFoundException(Long collectionId, Long postId) {
    super(String.format("Post %d is not in collection %d.", postId, collectionId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.COLLECTION_POST_NOT_FOUND;
  }
}
