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
class CommentLikeRepositoryTests {

  @Autowired private CommentLikeRepository commentLikeRepository;

  @Autowired private CommentRepository commentRepository;

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("좋아요를 두 번 눌러도 좋아요 수는 한 번만 증가한다")
  void insertAndIncrementIfAbsent_calledTwice_incrementsOnce() {
    Comment comment = saveComment("like-twice");
    User liker = saveUser("liker-twice");

    commentLikeRepository.insertAndIncrementIfAbsent(liker.getId(), comment.getId());
    commentLikeRepository.insertAndIncrementIfAbsent(liker.getId(), comment.getId());

    assertThat(reload(comment).getLikeCount()).isEqualTo(1);
    assertThat(commentLikeRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("좋아요를 취소하면 좋아요 수가 줄고, 없는 좋아요를 취소해도 음수가 되지 않는다")
  void deleteAndDecrementIfPresent_withoutLike_keepsCountAtZero() {
    Comment comment = saveComment("unlike");
    User liker = saveUser("liker-unlike");

    commentLikeRepository.insertAndIncrementIfAbsent(liker.getId(), comment.getId());
    commentLikeRepository.deleteAndDecrementIfPresent(liker.getId(), comment.getId());
    commentLikeRepository.deleteAndDecrementIfPresent(liker.getId(), comment.getId());

    assertThat(reload(comment).getLikeCount()).isZero();
    assertThat(commentLikeRepository.count()).isZero();
  }

  @Test
  @DisplayName("삭제된 댓글에는 좋아요를 남길 수 없다")
  void insertAndIncrementIfAbsent_deletedComment_doesNothing() {
    Comment comment = saveComment("deleted");
    comment.delete();
    commentRepository.saveAndFlush(comment);

    User liker = saveUser("liker-deleted");

    commentLikeRepository.insertAndIncrementIfAbsent(liker.getId(), comment.getId());

    assertThat(reload(comment).getLikeCount()).isZero();
    assertThat(commentLikeRepository.count()).isZero();
  }

  private Comment reload(Comment comment) {
    entityManager.flush();
    entityManager.clear();

    return commentRepository.findById(comment.getId()).orElseThrow();
  }

  private User saveUser(String key) {
    return userRepository.save(
        User.builder().email(key + "@example.com").fullName("댓글 사용자").build());
  }

  private Comment saveComment(String key) {
    User author = saveUser("author-" + key);
    Post post =
        Post.builder()
            .slug("comment-like-" + key)
            .author(author)
            .type(PostType.SHORT)
            .status(PostStatus.PUBLISHED)
            .jsonContent("댓글이 달린 게시글")
            .build();
    post.updateJsonContent("댓글이 달린 게시글", "댓글이 달린 게시글", 0);
    Post savedPost = postRepository.save(post);

    Comment comment = Comment.builder().author(author).post(savedPost).content("댓글 내용").build();
    Comment savedComment = commentRepository.saveAndFlush(comment);

    entityManager.flush();

    return savedComment;
  }
}
