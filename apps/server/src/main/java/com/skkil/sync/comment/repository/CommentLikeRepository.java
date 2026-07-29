package com.skkil.sync.comment.repository;

import com.skkil.sync.comment.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  @Modifying
  @Query(
      value =
          """
          WITH ins AS (
            INSERT INTO comment_likes (user_id, comment_id)
            SELECT :userId, :commentId
            FROM comments
            WHERE id = :commentId AND deleted_at IS NULL
            ON CONFLICT (user_id, comment_id) DO NOTHING
            RETURNING id
          )
          UPDATE comments SET like_count = like_count + 1
          WHERE id = :commentId AND EXISTS (SELECT 1 FROM ins)
          """,
      nativeQuery = true)
  void insertAndIncrementIfAbsent(Long userId, Long commentId);

  @Modifying
  @Query(
      value =
          """
          WITH del AS (
            DELETE FROM comment_likes
            WHERE user_id = :userId AND comment_id = :commentId
            RETURNING id
          )
          UPDATE comments SET like_count = GREATEST(like_count - 1, 0)
          WHERE id = :commentId AND EXISTS (SELECT 1 FROM del)
          """,
      nativeQuery = true)
  void deleteAndDecrementIfPresent(Long userId, Long commentId);
}
