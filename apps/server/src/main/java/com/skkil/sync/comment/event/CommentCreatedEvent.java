package com.skkil.sync.comment.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CommentCreatedEvent extends ApplicationEvent {

  private final Long commentId;
  private final Long postId;
  private final Long postAuthorId;
  private final Long commenterId;

  public CommentCreatedEvent(Long commentId, Long postId, Long postAuthorId, Long commenterId) {
    super(commentId);

    this.commentId = commentId;
    this.postId = postId;
    this.postAuthorId = postAuthorId;
    this.commenterId = commenterId;
  }
}
