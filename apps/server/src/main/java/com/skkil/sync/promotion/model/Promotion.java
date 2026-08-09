package com.skkil.sync.promotion.model;

import com.skkil.sync.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "promotions")
@Getter
public class Promotion extends BaseEntity {

  @Column(name = "active", nullable = false)
  private boolean active = false;

  // The post lives in whatever project it was authored into — set together at creation
  // time by the client (create the post first, then the promotion), never independently.
  // Title/description are deliberately not stored here — the linked post's own title and
  // content are the promotion's display content, so there is nothing to duplicate.
  @Column(name = "project_handle")
  private @Nullable String projectHandle;

  @Column(name = "post_slug")
  private @Nullable String postSlug;

  protected Promotion() {}

  @Builder
  public Promotion(@Nullable String projectHandle, @Nullable String postSlug) {
    this.projectHandle = projectHandle;
    this.postSlug = postSlug;
  }

  public void update(@Nullable String projectHandle, @Nullable String postSlug) {
    this.projectHandle = projectHandle;
    this.postSlug = postSlug;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}
