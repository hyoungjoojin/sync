package com.skkil.sync.user.service.oauth2;

import com.skkil.sync.user.constant.OAuth2Provider;
import com.skkil.sync.user.service.oauth2.userinfo.OAuth2UserDetails;
import com.skkil.sync.user.service.oauth2.userinfo.OAuth2UserInfoExtractor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final OAuth2AccountLinkingService oAuth2AccountLinkingService;
  private final Map<OAuth2Provider, OAuth2UserInfoExtractor> extractorsByProvider;

  public CustomOAuth2UserService(
      OAuth2AccountLinkingService oAuth2AccountLinkingService,
      List<OAuth2UserInfoExtractor> extractors) {
    this.oAuth2AccountLinkingService = oAuth2AccountLinkingService;
    this.extractorsByProvider =
        extractors.stream()
            .collect(Collectors.toMap(OAuth2UserInfoExtractor::getProvider, Function.identity()));
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2Provider provider = OAuth2Provider.from(registrationId);
    log.debug("Received OAuth2 user request for provider: {}", provider);

    OAuth2UserInfoExtractor extractor = extractorsByProvider.get(provider);
    if (extractor == null) {
      throw new IllegalStateException(
          "No OAuth2 user info extractor registered for provider: " + provider);
    }

    OAuth2User oAuth2User = super.loadUser(userRequest);
    OAuth2UserDetails userDetails = extractor.extract(oAuth2User.getAttributes());
    log.debug("OAuth2 user loaded: {}", userDetails.email());

    return oAuth2AccountLinkingService.resolveUser(provider, userDetails);
  }
}
