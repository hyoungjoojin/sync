package com.skkil.sync.collection.repository;

import static com.skkil.sync.jooq.tables.CollectionPosts.COLLECTION_POSTS;

import com.skkil.sync.collection.dto.data.CollectionPostDto;
import com.skkil.sync.common.util.pagination.interfaces.CursorPaginationDataFetcher;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionPostQueryRepository {

  private final DSLContext dsl;

  public CollectionPostQueryRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public CursorPaginationDataFetcher<CollectionPostDto> getCollectionPosts(Long collectionId) {
    return (condition, orderFields, size) ->
        dsl.select(
                COLLECTION_POSTS.ID.as("collectionPostId"),
                COLLECTION_POSTS.POST_ID.as("postId"),
                COLLECTION_POSTS.CREATED_AT.as("createdAt"))
            .from(COLLECTION_POSTS)
            .where(condition.and(COLLECTION_POSTS.COLLECTION_ID.eq(collectionId)))
            .orderBy(orderFields)
            .limit(size)
            .fetchInto(CollectionPostDto.class);
  }
}
