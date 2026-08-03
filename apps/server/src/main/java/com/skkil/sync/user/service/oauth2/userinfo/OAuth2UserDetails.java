package com.skkil.sync.user.service.oauth2.userinfo;

import org.jspecify.annotations.Nullable;

public record OAuth2UserDetails(
    String providerUserId, @Nullable String email, @Nullable String name) {}
