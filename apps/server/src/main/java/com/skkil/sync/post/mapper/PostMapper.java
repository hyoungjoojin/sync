package com.skkil.sync.post.mapper;

import com.skkil.sync.media.dto.MediaDto;
import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.dto.response.GetPostResponse;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.dto.summary.TagSummary;
import com.skkil.sync.post.security.PostAccessLevel;
import com.skkil.sync.project.dto.summary.ProjectSummary;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PostMapper {

  @Mapping(
      target = "scope",
      expression =
          "java(com.skkil.sync.post.model.PostScope.fromProjectHandle(post.projectHandle()))")
  PostSummary toPostSummary(
      PostDto post,
      PostAccessLevel accessLevel,
      UserSummary author,
      @Nullable ProjectSummary project,
      boolean isAuthor,
      List<TagSummary> tags,
      List<GetPostResponse.Media> previewMedia,
      @Nullable String coverImageUrl);

  List<GetPostResponse.Media> toPreviewMedia(List<MediaDto> media);

  @Mappings({
    @Mapping(target = "json", source = "post.content"),
    @Mapping(target = "media", source = "media")
  })
  GetPostResponse.Content toContent(PostDto post, List<MediaDto> media);

  @Mapping(target = "handle", source = "projectHandle")
  @Mapping(target = "name", source = "projectName")
  @Mapping(target = "description", source = "projectDescription")
  @Mapping(target = "website", source = "projectWebsite")
  @Mapping(target = "isPublic", source = "projectIsPublic")
  @Mapping(target = "joinPolicy", source = "projectJoinPolicy")
  @Mapping(target = "followerCount", source = "projectFollowerCount")
  @Mapping(target = "iconUrl", ignore = true)
  ProjectSummary toProjectSummary(PostDto post);
}
