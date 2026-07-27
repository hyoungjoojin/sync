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
@Table(name = "post_series_posts")
@Getter
public class PostSeriesPost extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_series_id", nullable = false)
  private PostSeries series;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(name = "position", nullable = false)
  private int position;

  protected PostSeriesPost() {}

  @Builder
  public PostSeriesPost(PostSeries series, Post post, int position) {
    this.series = series;
    this.post = post;
    this.position = position;
  }

  public void updatePosition(int position) {
    this.position = position;
  }
}
