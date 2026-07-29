package com.skkil.sync.post.security;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.security.CustomPermissionEvaluator;
import com.skkil.sync.common.security.PermissionOperation;
import com.skkil.sync.common.security.enums.PermissionEvaluatorType;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostPermissionEvaluator implements CustomPermissionEvaluator<Long> {

  private final PostRepository postRepository;
  private final TeammateRepository teammateRepository;

  public PostPermissionEvaluator(
      PostRepository postRepository, TeammateRepository teammateRepository) {
    this.postRepository = postRepository;
    this.teammateRepository = teammateRepository;
  }

  @Override
  public PermissionEvaluatorType type() {
    return PermissionEvaluatorType.POST;
  }

  @Override
  public boolean hasPermission(
      AuthenticatedUser user, Long targetId, PermissionOperation permission) {
    Post post = postRepository.findById(targetId).orElse(null);
    if (post == null) {
      log.debug("Post with ID {} not found", targetId);
      return false;
    }

    return switch (permission) {
      case READ -> canRead(user, post);
      case EDIT -> canEdit(user, post);
      case DELETE -> canDelete(user, post);

      default -> {
        log.debug("Unsupported permission operation: {}", permission);
        yield false;
      }
    };
  }

  private boolean canRead(AuthenticatedUser user, Post post) {
    if (!post.isVisible()) {
      return false;
    }

    boolean isAuthor = user != null && user.userId().equals(post.getAuthor().getId());

    // 프로젝트에 속하지 않은 개인 게시글: 기존 규칙(작성자 또는 공개)을 유지한다.
    if (post.getProject() == null) {
      if (!post.isPublished()) {
        return isAuthor;
      }
      return post.isPublic() || isAuthor;
    }

    // 워크스페이스(프로젝트) 게시글: 현재 프로젝트 팀원에게만 접근을 허용한다.
    // 작성자라도 프로젝트에서 나가거나 추방되면(팀원 레코드 삭제) 접근 권한을 잃는다.
    boolean isTeammate =
        user != null
            && teammateRepository
                .findByProjectIdAndUserId(post.getProject().getId(), user.userId())
                .isPresent();

    if (!post.isPublished()) {
      return isTeammate;
    }

    return post.getProject().isPublic() || isTeammate;
  }

  private boolean canEdit(AuthenticatedUser user, Post post) {
    if (user == null) {
      log.debug("Unauthenticated user cannot edit post");
      return false;
    }

    boolean isOwner = user.userId().equals(post.getAuthor().getId());
    if (!isOwner) {
      log.debug("User {} is not the owner of post {}, cannot edit", user.userId(), post.getId());
      return false;
    }

    return true;
  }

  private boolean canDelete(AuthenticatedUser user, Post post) {
    if (user == null) {
      log.debug("Unauthenticated user cannot delete post");
      return false;
    }

    if (user.userId().equals(post.getAuthor().getId())) {
      return true;
    }

    if (post.getProject() == null) {
      if (user.isAdmin()) {
        return true;
      }

      log.debug("User {} is not the author of post {}, cannot delete", user.userId(), post.getId());
      return false;
    }

    boolean canManageProject =
        teammateRepository
            .findByProjectIdAndUserId(post.getProject().getId(), user.userId())
            .map(Teammate::canManageProject)
            .orElse(false);
    if (!canManageProject) {
      log.debug(
          "User {} is neither the author of post {} nor a manager of its project, cannot delete",
          user.userId(),
          post.getId());
      return false;
    }

    return true;
  }
}
