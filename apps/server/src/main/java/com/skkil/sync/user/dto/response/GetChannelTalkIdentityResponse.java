package com.skkil.sync.user.dto.response;

import org.jspecify.annotations.Nullable;

public record GetChannelTalkIdentityResponse(String memberId, @Nullable String memberHash) {}
