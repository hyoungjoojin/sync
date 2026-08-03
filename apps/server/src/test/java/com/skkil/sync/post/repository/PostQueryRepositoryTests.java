package com.skkil.sync.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.common.config.TestcontainersConfig;
import com.skkil.sync.config.JpaConfig;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.ProjectRepository;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class, PostQueryRepository.class})
class PostQueryRepositoryTests {

  @Autowired private PostQueryRepository postQueryRepository;

  @Autowired private PostRepository postRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private TeammateRepository teammateRepository;

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("[getPostBySlug] 공개 프로젝트의 게시글은 팀원이 아니어도 조회된다")
  void getPostBySlug_publicProjectPost_visibleToNonTeammate() {
    User author = saveUser("public-project-author");
    User outsider = saveUser("public-project-outsider");
    Project project = saveProject("public-project", true);
    Post post = savePost("public-project-post", author, project);

    assertThat(postQueryRepository.getPostBySlug(outsider.getId(), post.getSlug())).isPresent();
    assertThat(postQueryRepository.getPostBySlug(null, post.getSlug())).isPresent();
  }

  @Test
  @DisplayName("[getPostBySlug] 비공개 프로젝트의 게시글은 팀원에게만 조회된다")
  void getPostBySlug_privateProjectPost_visibleToTeammateOnly() {
    User author = saveUser("private-project-author");
    User outsider = saveUser("private-project-outsider");
    Project project = saveProject("private-project", false);
    Post post = savePost("private-project-post", author, project);

    teammateRepository.saveAndFlush(Teammate.owner(project, author));

    assertThat(postQueryRepository.getPostBySlug(author.getId(), post.getSlug())).isPresent();
    assertThat(postQueryRepository.getPostBySlug(outsider.getId(), post.getSlug())).isEmpty();
    assertThat(postQueryRepository.getPostBySlug(null, post.getSlug())).isEmpty();
  }

  @Test
  @DisplayName("[getPinnedPostsByProject] 고정되지 않은 게시글은 제외하고, 비공개 프로젝트는 팀원에게만 노출한다")
  void getPinnedPostsByProject_returnsOnlyPinnedAndRespectsVisibility() {
    User author = saveUser("pinned-post-author");
    User outsider = saveUser("pinned-post-outsider");
    Project project = saveProject("pinned-project", false);
    teammateRepository.saveAndFlush(Teammate.owner(project, author));

    Post pinnedPost = savePost("pinned-post", author, project);
    pinnedPost.pin();
    postRepository.saveAndFlush(pinnedPost);
    savePost("unpinned-post", author, project);

    assertThat(postQueryRepository.getPinnedPostsByProject(author.getId(), project.getHandle()))
        .extracting(dto -> dto.id())
        .containsExactly(pinnedPost.getId());
    assertThat(postQueryRepository.getPinnedPostsByProject(outsider.getId(), project.getHandle()))
        .isEmpty();
  }

  private User saveUser(String key) {
    return userRepository.saveAndFlush(
        User.builder().email(key + "@example.com").fullName("사용자").build());
  }

  private Project saveProject(String handle, boolean isPublic) {
    return projectRepository.saveAndFlush(
        Project.builder().handle(handle).name(handle).isPublic(isPublic).build());
  }

  private Post savePost(String slug, User author, @Nullable Project project) {
    Post post =
        Post.builder()
            .slug(slug)
            .author(author)
            .project(project)
            .type(PostType.SHORT)
            .status(PostStatus.PUBLISHED)
            .content("본문")
            .build();
    post.updateContent("본문", "본문", 0);

    return postRepository.saveAndFlush(post);
  }
}
