package com.skkil.sync.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.common.config.TestcontainersConfig;
import com.skkil.sync.config.JpaConfig;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class})
class PostSearchRepositoryTests {

  @Autowired private PostSearchRepository postSearchRepository;

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("[findTopNByFullTextSearch] 검색어의 각 단어가 제목/본문에 나뉘어 있어도 모두 포함되면 조회된다")
  void findTopNByFullTextSearch_termsSplitAcrossTitleAndContent_matches() {
    User author = saveUser("search-author");
    Post match = savePost("oauth-guide", author, "OAuth Guide", "explains authentication flow");
    Post unrelated = savePost("cooking-notes", author, "Random Notes", "unrelated cooking recipe");

    var results = postSearchRepository.findTopNByFullTextSearch("authentication guide", null, 10);

    assertThat(results).contains(match.getId());
    assertThat(results).doesNotContain(unrelated.getId());
  }

  @Test
  @DisplayName("[findTopNByFullTextSearch] 검색어 중 일부 단어만 포함된 게시글은 조회되지 않는다")
  void findTopNByFullTextSearch_onlyPartialTermMatch_isExcluded() {
    User author = saveUser("search-partial-author");
    Post partialMatch =
        savePost("guide-only", author, "Setup Guide", "step by step setup instructions");

    var results = postSearchRepository.findTopNByFullTextSearch("authentication guide", null, 10);

    assertThat(results).doesNotContain(partialMatch.getId());
  }

  @Test
  @DisplayName("[findTopNByFullTextSearch] 한국어 검색어도 제목/본문에 나뉘어 있으면 조회된다")
  void findTopNByFullTextSearch_koreanTermsSplitAcrossTitleAndContent_matches() {
    User author = saveUser("search-ko-author");
    Post match = savePost("oauth-guide-ko", author, "OAuth 가이드", "사용자 인증 흐름을 설명합니다");
    Post unrelated = savePost("cooking-notes-ko", author, "임의의 메모", "관련 없는 요리 레시피입니다");

    var results = postSearchRepository.findTopNByFullTextSearch("인증 가이드", null, 10);

    assertThat(results).contains(match.getId());
    assertThat(results).doesNotContain(unrelated.getId());
  }

  @Test
  @DisplayName("[findTopNByFullTextSearch] 영어와 한국어가 섞인 검색어도 조회된다")
  void findTopNByFullTextSearch_mixedLanguageTerms_matches() {
    User author = saveUser("search-mixed-author");
    Post match =
        savePost("oauth-mixed", author, "사용자 인증 안내", "This OAuth flow 가이드 explains everything");
    Post unrelated =
        savePost("cooking-notes-mixed", author, "Random Notes", "unrelated cooking recipe");

    var results = postSearchRepository.findTopNByFullTextSearch("OAuth 가이드", null, 10);

    assertThat(results).contains(match.getId());
    assertThat(results).doesNotContain(unrelated.getId());
  }

  private User saveUser(String key) {
    return userRepository.saveAndFlush(
        User.builder().email(key + "@example.com").fullName("사용자").build());
  }

  private Post savePost(String slug, User author, String title, String content) {
    Post post = Post.builder().slug(slug).author(author).title(title).type(PostType.LONG).build();
    post.updateJsonContent(content, content, 0);

    return postRepository.saveAndFlush(post);
  }
}
