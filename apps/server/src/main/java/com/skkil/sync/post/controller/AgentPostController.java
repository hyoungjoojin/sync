package com.skkil.sync.post.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.auth.agent.AuthorizedAgentClient;
import com.skkil.sync.post.dto.request.CreateAgentPostRequest;
import com.skkil.sync.post.dto.response.AgentPostCreationResult;
import com.skkil.sync.post.service.AgentPostService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 에이전트 전용 엔드포인트. {@code /api/agent/**} 는 세션이 아니라 OAuth2 액세스 토큰으로 인증하는 별도의 필터 체인이 담당한다({@code
 * AgentResourceServerConfig}).
 *
 * <p>사용자 신원({@code principal})과 클라이언트 신원({@code client}) 모두 검증된 토큰의 클레임에서 나온다. 요청 본문에는 "누구로서" 쓸지를
 * 지정하는 필드가 없다.
 */
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
@RestController
public class AgentPostController {

  private final AgentPostService agentPostService;

  public AgentPostController(AgentPostService agentPostService) {
    this.agentPostService = agentPostService;
  }

  @PostMapping("/agent/posts")
  @ResponseStatus(HttpStatus.CREATED)
  public AgentPostCreationResult createPost(
      @AuthenticationPrincipal AuthenticatedUser user,
      AuthorizedAgentClient client,
      @RequestBody @Validated CreateAgentPostRequest request) {
    if (user == null) {
      throw new IllegalStateException("에이전트 요청에 인증된 사용자가 없습니다");
    }

    return agentPostService.createDraftPost(user.userId(), request, client);
  }
}
