package com.skkil.sync.comment.repository;

import static com.skkil.sync.jooq.tables.CommentLikes.COMMENT_LIKES;
import static com.skkil.sync.jooq.tables.Comments.COMMENTS;

import com.skkil.sync.comment.dto.data.CommentDto;
import com.skkil.sync.common.util.pagination.interfaces.CursorPaginationDataFetcher;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class CommentQueryRepository {

  private final DSLContext dsl;

  public CommentQueryRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public CursorPaginationDataFetcher<CommentDto> getCommentsByPost(
      Long postId, @Nullable Long requesterId) {
    Field<Boolean> liked =
        requesterId == null
            ? DSL.value(false)
            : DSL.field(
                DSL.exists(
                    DSL.selectOne()
                        .from(COMMENT_LIKES)
                        .where(COMMENT_LIKES.COMMENT_ID.eq(COMMENTS.ID))
                        .and(COMMENT_LIKES.USER_ID.eq(requesterId))));

    return (condition, orderFields, size) ->
        dsl.select(
                COMMENTS.ID.as("id"),
                COMMENTS.AUTHOR_ID.as("authorId"),
                COMMENTS.CONTENT.as("content"),
                COMMENTS.DELETED_AT.isNotNull().as("deleted"),
                COMMENTS.IS_ACCEPTED.as("accepted"),
                COMMENTS.LIKE_COUNT.as("likeCount"),
                liked.as("liked"),
                COMMENTS.CREATED_AT.as("createdAt"),
                COMMENTS.UPDATED_AT.as("updatedAt"))
            .from(COMMENTS)
            .where(condition.and(COMMENTS.POST_ID.eq(postId)))
            .orderBy(orderFields)
            .limit(size)
            .fetchInto(CommentDto.class);
  }
}
