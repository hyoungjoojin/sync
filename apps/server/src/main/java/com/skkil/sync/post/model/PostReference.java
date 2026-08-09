package com.skkil.sync.post.model;

import com.skkil.sync.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "post_references")
@Getter
public class PostReference extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_post_id", nullable = false)
  private Post sourcePost;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referenced_post_id", nullable = false)
  private Post referencedPost;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  protected PostReference() {}

  @Builder
  public PostReference(Post sourcePost, Post referencedPost, int sortOrder) {
    this.sourcePost = sourcePost;
    this.referencedPost = referencedPost;
    this.sortOrder = sortOrder;
  }
}
