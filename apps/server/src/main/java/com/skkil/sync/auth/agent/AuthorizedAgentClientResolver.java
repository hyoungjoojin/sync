package com.skkil.sync.auth.agent;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 컨트롤러의 {@link AuthorizedAgentClient} 파라미터를 검증된 액세스 토큰의 {@code client_id} 클레임에서 만들어 준다.
 *
 * <p>요청 본문이나 헤더에서 클라이언트를 읽지 않는다는 점이 핵심이다. 클라이언트 신원은 토큰에 서명되어 들어 있는 값이므로, 호출자가 다른 에이전트인 척할 수 없다.
 */
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class AuthorizedAgentClientResolver implements HandlerMethodArgumentResolver {

  private final RegisteredClientRepository registeredClientRepository;

  public AuthorizedAgentClientResolver(RegisteredClientRepository registeredClientRepository) {
    this.registeredClientRepository = registeredClientRepository;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return AuthorizedAgentClient.class.equals(parameter.getParameterType());
  }

  @Override
  public @Nullable Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    return current(registeredClientRepository);
  }

  /**
   * 현재 요청을 인증한 에이전트 클라이언트. MCP 도구 핸들러처럼 컨트롤러 파라미터가 아닌 곳에서도 필요하므로 정적 메서드로 열어 둔다.
   *
   * <p>등록 정보를 찾지 못하면(운영 중 클라이언트 행이 사라진 경우) 표시 이름 대신 식별자를 그대로 쓴다. 배지 문구 하나 때문에 글 작성을 실패시키지 않는다.
   */
  public static @Nullable AuthorizedAgentClient current(
      RegisteredClientRepository registeredClientRepository) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AgentAuthenticationToken agentToken)) {
      return null;
    }
    Jwt jwt = agentToken.getToken();

    String clientId = jwt.getClaimAsString("client_id");
    if (clientId == null) {
      return null;
    }

    RegisteredClient client = registeredClientRepository.findByClientId(clientId);
    if (client == null) {
      return new AuthorizedAgentClient(clientId, clientId);
    }

    return new AuthorizedAgentClient(client.getId(), client.getClientName());
  }
}
