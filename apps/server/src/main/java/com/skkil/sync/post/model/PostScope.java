package com.skkil.sync.post.model;

import com.skkil.sync.project.model.Project;
import org.jspecify.annotations.Nullable;

public enum PostScope {
  PUBLIC,
  WORKSPACE;

  public static PostScope fromProject(@Nullable Project project) {
    return project == null ? PUBLIC : WORKSPACE;
  }

  public static PostScope fromProjectHandle(@Nullable String projectHandle) {
    return projectHandle == null ? PUBLIC : WORKSPACE;
  }
}
