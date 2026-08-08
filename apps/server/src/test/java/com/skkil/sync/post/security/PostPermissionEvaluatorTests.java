package com.skkil.sync.post.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.security.PermissionOperation;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.model.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostPermissionEvaluatorTests {

  private static final Long POST_ID = 1L;
  private static final Long AUTHOR_ID = 2L;
  private static final Long OTHER_USER_ID = 3L;
  private static final Long PROJECT_ID = 4L;

  @Mock private PostRepository postRepository;

  @Mock private TeammateRepository teammateRepository;

  @InjectMocks private PostPermissionEvaluator permissionEvaluator;

  @Test
  @DisplayName("[hasPermission] 플랫폼 관리자는 개인 게시글을 삭제할 수 있음")
  void hasPermission_personalPost_delete_platformAdmin_returnsTrue() {
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(personalPost()));

    assertThat(
            permissionEvaluator.hasPermission(
                admin(OTHER_USER_ID), POST_ID, PermissionOperation.DELETE))
        .isTrue();
  }

  @Test
  @DisplayName("[hasPermission] 일반 사용자는 남의 개인 게시글을 삭제할 수 없음")
  void hasPermission_personalPost_delete_otherUser_returnsFalse() {
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(personalPost()));

    assertThat(
            permissionEvaluator.hasPermission(
                user(OTHER_USER_ID), POST_ID, PermissionOperation.DELETE))
        .isFalse();
  }

  @Test
  @DisplayName("[hasPermission] 플랫폼 관리자라도 개인 게시글을 수정할 수는 없음")
  void hasPermission_personalPost_edit_platformAdmin_returnsFalse() {
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(personalPost()));

    assertThat(
            permissionEvaluator.hasPermission(
                admin(OTHER_USER_ID), POST_ID, PermissionOperation.EDIT))
        .isFalse();
  }

  @Test
  @DisplayName("[hasPermission] 프로젝트 관리자는 해당 프로젝트의 게시글을 삭제할 수 있음")
  void hasPermission_projectPost_delete_projectManager_returnsTrue() {
    Project project = project();
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(projectPost(project)));
    when(teammateRepository.findByProjectIdAndUserId(PROJECT_ID, OTHER_USER_ID))
        .thenReturn(Optional.of(Teammate.owner(project, new User(OTHER_USER_ID))));

    assertThat(
            permissionEvaluator.hasPermission(
                user(OTHER_USER_ID), POST_ID, PermissionOperation.DELETE))
        .isTrue();
  }

  @Test
  @DisplayName("[hasPermission] 프로젝트 일반 팀원은 남의 프로젝트 게시글을 삭제할 수 없음")
  void hasPermission_projectPost_delete_projectMember_returnsFalse() {
    Project project = project();
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(projectPost(project)));
    when(teammateRepository.findByProjectIdAndUserId(PROJECT_ID, OTHER_USER_ID))
        .thenReturn(Optional.of(Teammate.member(project, new User(OTHER_USER_ID))));

    assertThat(
            permissionEvaluator.hasPermission(
                user(OTHER_USER_ID), POST_ID, PermissionOperation.DELETE))
        .isFalse();
  }

  @Test
  @DisplayName("[hasPermission] 팀원이 아니어도 공개 프로젝트의 게시글은 읽을 수 있음")
  void hasPermission_publicProjectPost_read_nonTeammate_returnsTrue() {
    Project project = project();
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(projectPost(project)));
    when(teammateRepository.findByProjectIdAndUserId(PROJECT_ID, OTHER_USER_ID))
        .thenReturn(Optional.empty());

    assertThat(
            permissionEvaluator.hasPermission(
                user(OTHER_USER_ID), POST_ID, PermissionOperation.READ))
        .isTrue();
  }

  @Test
  @DisplayName("[hasPermission] 팀원이 아니면 비공개 프로젝트의 게시글을 읽을 수 없음")
  void hasPermission_privateProjectPost_read_nonTeammate_returnsFalse() {
    Project privateProject =
        Project.builder().handle("project-handle").name("Project").isPublic(false).build();
    privateProject.setId(PROJECT_ID);
    when(postRepository.findByIdWithProject(POST_ID))
        .thenReturn(Optional.of(projectPost(privateProject)));
    when(teammateRepository.findByProjectIdAndUserId(PROJECT_ID, OTHER_USER_ID))
        .thenReturn(Optional.empty());

    assertThat(
            permissionEvaluator.hasPermission(
                user(OTHER_USER_ID), POST_ID, PermissionOperation.READ))
        .isFalse();
  }

  @Test
  @DisplayName("[hasPermission] 플랫폼 관리자는 다른 프로젝트의 게시글을 삭제할 수 없음")
  void hasPermission_projectPost_delete_platformAdmin_returnsFalse() {
    Project project = project();
    when(postRepository.findByIdWithProject(POST_ID)).thenReturn(Optional.of(projectPost(project)));
    when(teammateRepository.findByProjectIdAndUserId(PROJECT_ID, OTHER_USER_ID))
        .thenReturn(Optional.empty());

    assertThat(
            permissionEvaluator.hasPermission(
                admin(OTHER_USER_ID), POST_ID, PermissionOperation.DELETE))
        .isFalse();
  }

  private static Post personalPost() {
    return post(null);
  }

  private static Post projectPost(Project project) {
    return post(project);
  }

  private static Post post(Project project) {
    Post post =
        Post.builder()
            .slug("post-slug")
            .author(new User(AUTHOR_ID))
            .project(project)
            .jsonContent("{}")
            .type(PostType.SHORT)
            .build();
    post.setId(POST_ID);
    return post;
  }

  private static Project project() {
    Project project =
        Project.builder().handle("project-handle").name("Project").isPublic(true).build();
    project.setId(PROJECT_ID);
    return project;
  }

  private static AuthenticatedUser user(Long userId) {
    return AuthenticatedUser.builder().userId(userId).role(Role.USER).build();
  }

  private static AuthenticatedUser admin(Long userId) {
    return AuthenticatedUser.builder().userId(userId).role(Role.ADMIN).build();
  }
}
