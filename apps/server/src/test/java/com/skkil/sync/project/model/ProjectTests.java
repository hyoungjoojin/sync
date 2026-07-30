package com.skkil.sync.project.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skkil.sync.project.exception.ProjectJoinPolicyNotAllowedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectTests {

  @ParameterizedTest
  @EnumSource(
      value = JoinPolicy.class,
      names = {"OPEN", "REQUEST"})
  @DisplayName("[생성] 비공개 프로젝트는 INVITE 이외의 참여 정책을 가질 수 없다")
  void create_privateProjectWithNonInviteJoinPolicy_throws(JoinPolicy joinPolicy) {
    assertThatThrownBy(() -> createProject(false, joinPolicy))
        .isInstanceOf(ProjectJoinPolicyNotAllowedException.class);
  }

  @Test
  @DisplayName("[생성] 비공개 프로젝트는 INVITE 참여 정책을 가질 수 있다")
  void create_privateProjectWithInviteJoinPolicy_succeeds() {
    Project project = createProject(false, JoinPolicy.INVITE);

    assertThat(project.getJoinPolicy()).isEqualTo(JoinPolicy.INVITE);
  }

  @Test
  @DisplayName("[생성] 비공개 프로젝트는 참여 정책을 지정하지 않으면 INVITE가 된다")
  void create_privateProjectWithoutJoinPolicy_defaultsToInvite() {
    Project project = createProject(false, null);

    assertThat(project.getJoinPolicy()).isEqualTo(JoinPolicy.INVITE);
  }

  @ParameterizedTest
  @EnumSource(JoinPolicy.class)
  @DisplayName("[생성] 공개 프로젝트는 모든 참여 정책을 가질 수 있다")
  void create_publicProjectWithAnyJoinPolicy_succeeds(JoinPolicy joinPolicy) {
    assertThatCode(() -> createProject(true, joinPolicy)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(
      value = JoinPolicy.class,
      names = {"OPEN", "REQUEST"})
  @DisplayName("[참여 정책 변경] 비공개 프로젝트는 INVITE 이외의 정책으로 변경할 수 없다")
  void updateJoinPolicy_privateProjectToNonInvite_throws(JoinPolicy joinPolicy) {
    Project project = createProject(false, JoinPolicy.INVITE);

    assertThatThrownBy(() -> project.updateJoinPolicy(joinPolicy))
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
