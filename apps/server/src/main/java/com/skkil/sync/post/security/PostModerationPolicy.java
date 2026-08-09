package com.skkil.sync.post.security;

import com.skkil.sync.post.dto.data.PostDto;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PostModerationPolicy {

  private final UserRepository userRepository;
  private final TeammateRepository teammateRepository;

  public PostModerationPolicy(
      UserRepository userRepository, TeammateRepository teammateRepository) {
    this.userRepository = userRepository;
    this.teammateRepository = teammateRepository;
  }

  public Map<Long, Boolean> resolveDeletable(@Nullable Long requesterId, List<PostDto> posts) {
    if (requesterId == null) {
      return posts.stream().collect(Collectors.toMap(PostDto::id, post -> false, (a, b) -> a));
    }

    Set<String> projectHandles =
        posts.stream()
            .filter(post -> !requesterId.equals(post.authorId()))
            .map(PostDto::projectHandle)
            .filter(handle -> handle != null)
            .collect(Collectors.toSet());

    boolean hasForeignPersonalPost =
        posts.stream()
            .anyMatch(post -> !requesterId.equals(post.authorId()) && post.projectHandle() == null);

    boolean isPlatformAdmin = hasForeignPersonalPost && isPlatformAdmin(requesterId);
    Set<String> managedHandles = managedProjectHandles(requesterId, projectHandles);

    return posts.stream()
        .collect(
            Collectors.toMap(
                PostDto::id,
                post -> {
                  if (requesterId.equals(post.authorId())) {
                    return true;
                  }

                  if (post.projectHandle() == null) {
                    return isPlatformAdmin;
                  }

                  return managedHandles.contains(post.projectHandle());
                },
                (a, b) -> a));
  }

  private boolean isPlatformAdmin(Long requesterId) {
    return userRepository
        .findById(requesterId)
        .map(User::getRole)
        .filter(role -> role == Role.ADMIN)
        .isPresent();
  }

  private Set<String> managedProjectHandles(Long requesterId, Set<String> projectHandles) {
    if (projectHandles.isEmpty()) {
      return Set.of();
    }

    return teammateRepository.findByUserIdAndProjectHandleIn(requesterId, projectHandles).stream()
        .filter(Teammate::canManageProject)
        .map(teammate -> teammate.getProject().getHandle())
        .collect(Collectors.toSet());
  }
}
