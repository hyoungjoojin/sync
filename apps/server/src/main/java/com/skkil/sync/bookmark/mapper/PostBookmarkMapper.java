package com.skkil.sync.bookmark.mapper;

import com.skkil.sync.bookmark.dto.data.BookmarkedPostDto;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.project.dto.summary.ProjectSummary;
import com.skkil.sync.user.dto.summary.UserSummary;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostBookmarkMapper {

  PostSummary toPostSummary(
      BookmarkedPostDto post,
      UserSummary author,
      @Nullable ProjectSummary project,
      boolean isAuthor);

  @Mapping(target = "handle", source = "projectHandle")
  @Mapping(target = "name", source = "projectName")
  @Mapping(target = "description", source = "projectDescription")
  @Mapping(target = "website", source = "projectWebsite")
  @Mapping(target = "isPublic", source = "projectIsPublic")
  ProjectSummary toProjectSummary(BookmarkedPostDto post);
}
