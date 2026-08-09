package com.skkil.sync.collection.exception;

import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.exception.SyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CollectionNotFoundException extends SyncException {

  public CollectionNotFoundException(Long collectionId) {
    super(String.format("Collection with id %d not found.", collectionId));
  }

  public CollectionNotFoundException(String externalId) {
    super(String.format("Collection with external id '%s' not found.", externalId));
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.COLLECTION_NOT_FOUND;
  }
}
