package com.skkil.sync.post.security;

import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PostCommentPolicy {

  private final TeammateRepository teammateRepository;

  public PostCommentPolicy(TeammateRepository teammateRepository) {
    this.teammateRepository = teammateRepository;
  }

  public boolean canComment(@Nullable Long requesterId, PostDto post) {
    return resolveCommentable(requesterId, List.of(post)).getOrDefault(post.id(), false);
  }

  public Map<Long, Boolean> resolveCommentable(@Nullable Long requesterId, List<PostDto> posts) {
    if (requesterId == null) {
      return posts.stream().collect(Collectors.toMap(PostDto::id, post -> false, (a, b) -> a));
    }

    Set<String> projectHandles =
        posts.stream()
            .filter(PostCommentPolicy::isCommentablePost)
            .map(PostDto::projectHandle)
            .filter(handle -> handle != null)
            .collect(Collectors.toSet());

    Set<String> joinedHandles = joinedProjectHandles(requesterId, projectHandles);

    return posts.stream()
        .collect(
            Collectors.toMap(
                PostDto::id,
                post -> {
                  if (!isCommentablePost(post)) {
                    return false;
                  }

                  if (post.projectHandle() == null) {
                    return true;
                  }

                  return joinedHandles.contains(post.projectHandle());
                },
                (a, b) -> a));
  }

  private static boolean isCommentablePost(PostDto post) {
    return post.status() == PostStatus.PUBLISHED;
  }

  private Set<String> joinedProjectHandles(Long requesterId, Set<String> projectHandles) {
    if (projectHandles.isEmpty()) {
      return Set.of();
    }

    return teammateRepository.findByUserIdAndProjectHandleIn(requesterId, projectHandles).stream()
        .map(Teammate::getProject)
        .map(project -> project.getHandle())
        .collect(Collectors.toSet());
  }
}
