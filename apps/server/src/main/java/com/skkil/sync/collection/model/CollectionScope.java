package com.skkil.sync.collection.model;

import com.skkil.sync.project.model.Project;

public enum CollectionScope {
  PERSONAL,
  WORKSPACE;

  public static CollectionScope fromProject(Project project) {
    return project == null ? PERSONAL : WORKSPACE;
  }
}
