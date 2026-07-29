package com.skkil.sync.user.service;

import com.skkil.sync.common.integration.channeltalk.ChannelTalkService;
import com.skkil.sync.user.dto.response.GetChannelTalkIdentityResponse;
import org.springframework.stereotype.Service;

@Service
public class ChannelTalkIdentityService {

  private final ChannelTalkService channelTalkService;

  public ChannelTalkIdentityService(ChannelTalkService channelTalkService) {
    this.channelTalkService = channelTalkService;
  }

  public GetChannelTalkIdentityResponse getChannelTalkIdentity(Long userId) {
    String memberId = String.valueOf(userId);

    return new GetChannelTalkIdentityResponse(
        memberId, channelTalkService.generateMemberHash(memberId));
  }
}
