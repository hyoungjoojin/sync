package com.skkil.sync.auth.agent;

import com.skkil.sync.auth.AuthenticatedUser;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

/**
 * 액세스 토큰으로 인증된 에이전트 요청의 인증 객체. 주체는 SYNC 의 {@link AuthenticatedUser} 이고, 원본 JWT 를 그대로 들고 있다.
 *
 * <p>{@code UsernamePasswordAuthenticationToken} 을 쓰면 안 된다. 그 타입은 {@code CredentialsContainer} 라서
 * {@code ProviderManager} 가 인증 직후 자격 증명을 지워 버리고, 그러면 뒤에서 {@code client_id} 클레임을 읽을 수 없다 — 어떤 에이전트가
 * 만든 글인지 기록하지 못하게 된다. {@code AbstractOAuth2TokenAuthenticationToken} 은 토큰을 지우지 않는다.
 */
public class AgentAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {

  private final AuthenticatedUser principal;

  public AgentAuthenticationToken(
      Jwt token, AuthenticatedUser principal, Collection<? extends GrantedAuthority> authorities) {
    super(token, principal, token, authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public AuthenticatedUser getPrincipal() {
    return principal;
  }

  @Override
  public java.util.Map<String, Object> getTokenAttributes() {
    return getToken().getClaims();
  }
}
