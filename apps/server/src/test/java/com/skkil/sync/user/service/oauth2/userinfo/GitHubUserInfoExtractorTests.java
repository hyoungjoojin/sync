package com.skkil.sync.user.service.oauth2.userinfo;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.user.constant.OAuth2Provider;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GitHubUserInfoExtractorTests {

  private final GitHubUserInfoExtractor extractor = new GitHubUserInfoExtractor();

  @Test
  void getProvider_returnsGitHub() {
    assertThat(extractor.getProvider()).isEqualTo(OAuth2Provider.GITHUB);
  }

  @Test
  void extract_withPublicEmail_usesAttributesDirectly() {
    Map<String, Object> attributes =
        Map.of(
            "id", 123456,
            "login", "octocat",
            "name", "The Octocat",
            "email", "octocat@github.com");

    OAuth2UserDetails result = extractor.extract(attributes, "access-token");

    assertThat(result.providerUserId()).isEqualTo("123456");
    assertThat(result.email()).isEqualTo("octocat@github.com");
    assertThat(result.name()).isEqualTo("The Octocat");
  }

  @Test
  void extract_withoutName_fallsBackToLogin() {
    Map<String, Object> attributes = Map.of("id", 123456, "login", "octocat");

    OAuth2UserDetails result = extractor.extract(attributes, "access-token");

    assertThat(result.name()).isEqualTo("octocat");
  }
}
