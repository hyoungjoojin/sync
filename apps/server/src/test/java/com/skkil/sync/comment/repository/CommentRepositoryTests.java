package com.skkil.sync.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.comment.model.Comment;
import com.skkil.sync.common.config.TestcontainersConfig;
import com.skkil.sync.config.JpaConfig;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class})
class CommentRepositoryTests {

  @Autowired private CommentRepository commentRepository;

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("댓글을 채택하면 게시글이 해결 상태가 된다")
  void syncResolvedFromAcceptedComments_accepted_marksPostResolved() {
    Post post = savePost("accept");
    Comment comment = saveComment(post, "답변");

    accept(comment);
    commentRepository.syncResolvedFromAcceptedComments(post.getId());

    assertThat(reloadPost(post).isResolved()).isTrue();
  }

  @Test
  @DisplayName("채택된 댓글이 여러 개면 하나를 채택 취소해도 해결 상태가 유지된다")
  void syncResolvedFromAcceptedComments_multipleAccepted_staysResolved() {
    Post post = savePost("multiple");
    Comment first = saveComment(post, "첫 번째 답변");
    Comment second = saveComment(post, "두 번째 답변");

    accept(first);
    accept(second);
    commentRepository.syncResolvedFromAcceptedComments(post.getId());
    assertThat(reloadPost(post).isResolved()).isTrue();

    unaccept(first);
    commentRepository.syncResolvedFromAcceptedComments(post.getId());
    assertThat(reloadPost(post).isResolved()).isTrue();

    unaccept(second);
    commentRepository.syncResolvedFromAcceptedComments(post.getId());
    assertThat(reloadPost(post).isResolved()).isFalse();
  }

  @Test
  @DisplayName("채택된 댓글이 삭제되면 게시글의 해결 상태가 해제된다")
  void syncResolvedFromAcceptedComments_acceptedCommentDeleted_clearsResolved() {
    Post post = savePost("deleted");
    Comment comment = saveComment(post, "삭제될 답변");

    accept(comment);
    commentRepository.syncResolvedFromAcceptedComments(post.getId());
    assertThat(reloadPost(post).isResolved()).isTrue();

    commentRepository.softDeleteAndDecrementIfPresent(comment.getId());
    commentRepository.syncResolvedFromAcceptedComments(post.getId());

    assertThat(reloadPost(post).isResolved()).isFalse();
  }

  private void accept(Comment comment) {
    Comment managed = commentRepository.findById(comment.getId()).orElseThrow();
    managed.accept();
    commentRepository.saveAndFlush(managed);
  }

  private void unaccept(Comment comment) {
    Comment managed = commentRepository.findById(comment.getId()).orElseThrow();
    managed.unaccept();
    commentRepository.saveAndFlush(managed);
  }

  private Post reloadPost(Post post) {
    entityManager.flush();
    entityManager.clear();

    return postRepository.findById(post.getId()).orElseThrow();
  }

  private User saveUser(String key) {
    return userRepository.save(
        User.builder().email(key + "@example.com").fullName("질문 사용자").build());
  }

  private Post savePost(String key) {
    User author = saveUser("author-resolved-" + key);
    Post post =
        Post.builder()
            .slug("comment-accept-" + key)
            .author(author)
            .type(PostType.QUESTION)
            .status(PostStatus.PUBLISHED)
            .content("질문 본문")
            .build();
    post.updateContent("질문 본문", "질문 본문", 0);

    Post savedPost = postRepository.saveAndFlush(post);
    entityManager.flush();

    return savedPost;
  }

  private Comment saveComment(Post post, String content) {
    User author = saveUser("commenter-" + post.getSlug() + "-" + content.hashCode());
    Comment comment = Comment.builder().author(author).post(post).content(content).build();

    Comment savedComment = commentRepository.saveAndFlush(comment);
    entityManager.flush();

    return savedComment;
  }
}
