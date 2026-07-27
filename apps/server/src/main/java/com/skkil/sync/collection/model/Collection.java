package com.skkil.sync.collection.model;

import com.skkil.sync.common.domain.BaseEntity;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "collections")
@Getter
public class Collection extends BaseEntity {

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private @Nullable Project project;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private @Nullable String description;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic = true;

  @Column(name = "post_count", nullable = false)
  private long postCount = 0;

  protected Collection() {}

  @Builder
  public Collection(
      String externalId,
      User creator,
      @Nullable Project project,
      String name,
      @Nullable String description,
      boolean isPublic) {
    this.externalId = externalId;
    this.creator = creator;
    this.project = project;
    this.name = name;
    this.description = description;
    this.isPublic = isPublic;
  }

  public void update(String name, @Nullable String description, boolean isPublic) {
    this.name = name;
    this.description = description;
    this.isPublic = isPublic;
  }

  public void incrementPostCount() {
    this.postCount++;
  }

  public void decrementPostCount() {
    if (this.postCount > 0) {
      this.postCount--;
    }
  }

  public CollectionScope getScope() {
    return CollectionScope.fromProject(project);
  }

  public boolean isPersonal() {
    return getScope() == CollectionScope.PERSONAL;
  }

  public boolean isWorkspace() {
    return getScope() == CollectionScope.WORKSPACE;
  }
}
