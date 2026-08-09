package com.skkil.sync.user.service.oauth2;

import com.skkil.sync.user.constant.OAuth2Provider;
import com.skkil.sync.user.service.oauth2.userinfo.OAuth2UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {

  private final OAuth2AccountLinkingService oAuth2AccountLinkingService;

  public CustomOidcUserService(OAuth2AccountLinkingService oAuth2AccountLinkingService) {
    this.oAuth2AccountLinkingService = oAuth2AccountLinkingService;
  }

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2Provider provider = OAuth2Provider.from(registrationId);
    log.debug("Received OIDC user request for provider: {}", provider);

    OidcUser oidcUser = super.loadUser(userRequest);
    log.debug("OIDC user loaded: {}", oidcUser.getEmail());

    OAuth2UserDetails userDetails =
        new OAuth2UserDetails(oidcUser.getSubject(), oidcUser.getEmail(), oidcUser.getFullName());
    return oAuth2AccountLinkingService.resolveUser(provider, userDetails);
  }
}
