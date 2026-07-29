package com.skkil.sync.comment.repository;

import com.skkil.sync.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Modifying
  @Query(
      value =
          """
          UPDATE posts SET comment_count = comment_count + 1
          WHERE id = :postId
          """,
      nativeQuery = true)
  void incrementCommentCount(Long postId);

  @Modifying
  @Query(
      value =
          """
          WITH del AS (
            UPDATE comments
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = :commentId AND deleted_at IS NULL
            RETURNING post_id
          )
          UPDATE posts SET comment_count = GREATEST(comment_count - 1, 0)
          WHERE id = (SELECT post_id FROM del)
          """,
      nativeQuery = true)
  void softDeleteAndDecrementIfPresent(Long commentId);

  @Modifying(flushAutomatically = true)
  @Query(
      value =
          """
          UPDATE posts p
          SET resolved = accepted.exists_accepted
          FROM (
            SELECT EXISTS (
              SELECT 1 FROM comments
              WHERE post_id = :postId AND is_accepted = TRUE AND deleted_at IS NULL
            ) AS exists_accepted
          ) accepted
          WHERE p.id = :postId AND p.resolved <> accepted.exists_accepted
          """,
      nativeQuery = true)
  void syncResolvedFromAcceptedComments(Long postId);
}
