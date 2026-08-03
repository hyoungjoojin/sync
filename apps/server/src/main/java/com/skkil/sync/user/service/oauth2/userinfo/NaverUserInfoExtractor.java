package com.skkil.sync.user.service.oauth2.userinfo;

import com.skkil.sync.user.constant.OAuth2Provider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NaverUserInfoExtractor implements OAuth2UserInfoExtractor {

  @Override
  public OAuth2Provider getProvider() {
    return OAuth2Provider.NAVER;
  }

  @Override
  @SuppressWarnings("unchecked")
  public OAuth2UserDetails extract(Map<String, Object> attributes) {
    Object response = attributes.get("response");
    if (!(response instanceof Map)) {
      throw new IllegalArgumentException(
          "Naver OAuth2 response is missing the expected 'response' object.");
    }

    Map<String, Object> profile = (Map<String, Object>) response;
    return new OAuth2UserDetails(
        (String) profile.get("id"), (String) profile.get("email"), (String) profile.get("name"));
  }
}
