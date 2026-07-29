package com.skkil.sync.user.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.user.dto.response.GetChannelTalkIdentityResponse;
import com.skkil.sync.user.service.ChannelTalkIdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChannelTalkIdentityController {

  private final ChannelTalkIdentityService channelTalkIdentityService;

  public ChannelTalkIdentityController(ChannelTalkIdentityService channelTalkIdentityService) {
    this.channelTalkIdentityService = channelTalkIdentityService;
  }

  @GetMapping("/channel-talk/identity")
  @ResponseStatus(HttpStatus.OK)
  public GetChannelTalkIdentityResponse getChannelTalkIdentity(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return channelTalkIdentityService.getChannelTalkIdentity(user.userId());
  }
}
