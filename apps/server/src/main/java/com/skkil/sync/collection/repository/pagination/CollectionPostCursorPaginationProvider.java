package com.skkil.sync.collection.repository.pagination;

import static com.skkil.sync.jooq.tables.CollectionPosts.COLLECTION_POSTS;

import com.skkil.sync.collection.dto.data.CollectionPostCursor;
import com.skkil.sync.collection.dto.data.CollectionPostDto;
import com.skkil.sync.common.util.pagination.keyset.KeysetCursorPaginationProvider;
import com.skkil.sync.common.util.pagination.keyset.KeysetField;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CollectionPostCursorPaginationProvider
    extends KeysetCursorPaginationProvider<CollectionPostDto, CollectionPostCursor> {

  @Override
  public Class<CollectionPostCursor> getCursorClass() {
    return CollectionPostCursor.class;
  }

  @Override
  protected List<KeysetField<CollectionPostCursor, ?>> getKeysetFields() {
    return List.of(
        KeysetField.asc(COLLECTION_POSTS.CREATED_AT, CollectionPostCursor::createdAt),
        KeysetField.asc(COLLECTION_POSTS.ID, CollectionPostCursor::id));
  }

  @Override
  public CollectionPostCursor convert(CollectionPostDto entity) {
    return new CollectionPostCursor(entity.createdAt(), entity.collectionPostId());
  }
}
