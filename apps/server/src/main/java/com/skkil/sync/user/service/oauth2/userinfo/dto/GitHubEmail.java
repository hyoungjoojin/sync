package com.skkil.sync.user.service.oauth2.userinfo.dto;

public record GitHubEmail(String email, boolean primary, boolean verified) {}
