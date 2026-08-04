package com.skkil.sync.user.service.oauth2.userinfo;

import com.skkil.sync.user.constant.OAuth2Provider;
import java.util.Map;

public interface OAuth2UserInfoExtractor {

  OAuth2Provider getProvider();

  OAuth2UserDetails extract(Map<String, Object> attributes, String accessToken);
}
