package com.skkil.sync.user.service.oauth2.userinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skkil.sync.user.constant.OAuth2Provider;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NaverUserInfoExtractorTests {

  private final NaverUserInfoExtractor extractor = new NaverUserInfoExtractor();

  @Test
  void getProvider_returnsNaver() {
    assertThat(extractor.getProvider()).isEqualTo(OAuth2Provider.NAVER);
  }

  @Test
  void extract_unwrapsNestedResponseObject() {
    Map<String, Object> attributes =
        Map.of(
            "resultcode",
            "00",
            "message",
            "success",
            "response",
            Map.of(
                "id", "naver-user-id-123",
                "email", "user@naver.com",
                "name", "User Name"));

    OAuth2UserDetails result = extractor.extract(attributes, "access-token");

    assertThat(result.providerUserId()).isEqualTo("naver-user-id-123");
    assertThat(result.email()).isEqualTo("user@naver.com");
    assertThat(result.name()).isEqualTo("User Name");
  }

  @Test
  void extract_missingResponseObject_throwsIllegalArgumentException() {
    Map<String, Object> attributes = Map.of("resultcode", "00", "message", "success");

    assertThatThrownBy(() -> extractor.extract(attributes, "access-token"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
