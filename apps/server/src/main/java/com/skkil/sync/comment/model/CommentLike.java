package com.skkil.sync.comment.model;

import com.skkil.sync.common.domain.BaseEntity;
import com.skkil.sync.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(
    name = "comment_likes",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "comment_id"})})
@Getter
public class CommentLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;

  protected CommentLike() {}

  @Builder
  public CommentLike(User user, Comment comment) {
    this.user = user;
    this.comment = comment;
  }
}
