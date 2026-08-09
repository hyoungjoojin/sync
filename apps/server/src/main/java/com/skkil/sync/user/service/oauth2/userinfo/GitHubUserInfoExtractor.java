package com.skkil.sync.user.service.oauth2.userinfo;

import com.skkil.sync.user.constant.OAuth2Provider;
import com.skkil.sync.user.service.oauth2.userinfo.dto.GitHubEmail;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class GitHubUserInfoExtractor implements OAuth2UserInfoExtractor {

  private static final String EMAILS_URL = "https://api.github.com/user/emails";

  private final RestClient restClient;

  public GitHubUserInfoExtractor() {
    this.restClient = RestClient.builder().build();
  }

  @Override
  public OAuth2Provider getProvider() {
    return OAuth2Provider.GITHUB;
  }

  @Override
  public OAuth2UserDetails extract(Map<String, Object> attributes, String accessToken) {
    String providerUserId = String.valueOf(attributes.get("id"));
    String name = (String) attributes.get("name");
    if (name == null) {
      name = (String) attributes.get("login");
    }

    String email = (String) attributes.get("email");
    if (email == null) {
      email = fetchPrimaryVerifiedEmail(accessToken);
    }

    return new OAuth2UserDetails(providerUserId, email, name);
  }

  private @Nullable String fetchPrimaryVerifiedEmail(String accessToken) {
    List<GitHubEmail> emails;
    try {
      emails =
          restClient
              .get()
              .uri(EMAILS_URL)
              .headers(headers -> headers.setBearerAuth(accessToken))
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(new ParameterizedTypeReference<List<GitHubEmail>>() {});
    } catch (RestClientException e) {
      log.warn("Failed to fetch GitHub user emails: {}", e.getMessage());
      return null;
    }

    if (emails == null) {
      return null;
    }

    return emails.stream()
        .filter(GitHubEmail::primary)
        .filter(GitHubEmail::verified)
        .map(GitHubEmail::email)
        .findFirst()
        .orElse(null);
  }
}
