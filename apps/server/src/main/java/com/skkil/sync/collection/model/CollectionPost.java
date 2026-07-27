package com.skkil.sync.collection.model;

import com.skkil.sync.common.domain.BaseEntity;
import com.skkil.sync.post.model.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "collection_posts")
@Getter
public class CollectionPost extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collection_id", nullable = false)
  private Collection collection;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private @Nullable Post post;

  protected CollectionPost() {}

  @Builder
  public CollectionPost(Collection collection, @Nullable Post post) {
    this.collection = collection;
    this.post = post;
  }

  public boolean isDeleted() {
    return this.post == null;
  }
}
