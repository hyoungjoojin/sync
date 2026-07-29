package com.skkil.sync.project.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skkil.sync.project.exception.ProjectJoinPolicyNotAllowedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectTests {

  @Test
  @DisplayName("[생성] 비공개 프로젝트는 OPEN 참여 정책을 가질 수 없다")
  void create_privateProjectWithOpenJoinPolicy_throws() {
    assertThatThrownBy(() -> createProject(false, JoinPolicy.OPEN))
        .isInstanceOf(ProjectJoinPolicyNotAllowedException.class);
  }

  @Test
  @DisplayName("[생성] 공개 프로젝트는 OPEN 참여 정책을 가질 수 있다")
  void create_publicProjectWithOpenJoinPolicy_succeeds() {
    Project project = createProject(true, JoinPolicy.OPEN);

    assertThat(project.getJoinPolicy()).isEqualTo(JoinPolicy.OPEN);
  }

  @Test
  @DisplayName("[생성] 비공개 프로젝트는 REQUEST/INVITE 참여 정책을 가질 수 있다")
  void create_privateProjectWithoutOpenJoinPolicy_succeeds() {
    assertThatCode(() -> createProject(false, JoinPolicy.REQUEST)).doesNotThrowAnyException();
    assertThatCode(() -> createProject(false, JoinPolicy.INVITE)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("[참여 정책 변경] 비공개 프로젝트는 OPEN으로 변경할 수 없다")
  void updateJoinPolicy_privateProjectToOpen_throws() {
    Project project = createProject(false, JoinPolicy.INVITE);

    assertThatThrownBy(() -> project.updateJoinPolicy(JoinPolicy.OPEN))
        .isInstanceOf(ProjectJoinPolicyNotAllowedException.class);
    assertThat(project.getJoinPolicy()).isEqualTo(JoinPolicy.INVITE);
  }

  @Test
  @DisplayName("[참여 정책 변경] 공개 프로젝트는 OPEN으로 변경할 수 있다")
  void updateJoinPolicy_publicProjectToOpen_succeeds() {
    Project project = createProject(true, JoinPolicy.INVITE);

    project.updateJoinPolicy(JoinPolicy.OPEN);

    assertThat(project.getJoinPolicy()).isEqualTo(JoinPolicy.OPEN);
  }

  private static Project createProject(boolean isPublic, JoinPolicy joinPolicy) {
    return Project.builder()
        .handle("test-project")
        .name("테스트 프로젝트")
        .isPublic(isPublic)
        .joinPolicy(joinPolicy)
        .build();
  }
}
