package com.skkil.sync.auth.agent;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.user.service.UserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * 검증이 끝난 액세스 토큰을 SYNC 의 {@link AuthenticatedUser} 주체로 바꾼다. 이 한 단계 덕분에 {@code @PreAuthorize} 와
 * {@code hasPermission} SpEL 은 요청이 세션으로 왔는지 토큰으로 왔는지 전혀 모르는 채로 그대로 동작한다.
 *
 * <p>사용자 신원은 오직 토큰의 {@code sub} 클레임에서만 나온다. 요청 본문이나 헤더에 담긴 어떤 값도 "누구로 행세할지"에 영향을 주지 못한다. {@code
 * sub} 는 인가 흐름을 마친 주체의 이름, 즉 SYNC 사용자의 이메일이다.
 *
 * <p>서명과 만료는 리소스 서버가 이 변환기보다 먼저 검사한다. 여기서는 해시 조회도, 폐기/만료 확인도 하지 않는다.
 */
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class AgentJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String SCOPE_CLAIM = "scope";

  private final UserService userService;

  public AgentJwtAuthenticationConverter(UserService userService) {
    this.userService = userService;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    AuthenticatedUser principal = userService.loadUserByUsername(jwt.getSubject());

    return new AgentAuthenticationToken(jwt, principal, authorities(jwt, principal));
  }

  /**
   * 사용자 본래의 권한(예: ROLE_ADMIN)과 토큰이 지닌 스코프를 합친다. 스코프는 {@code SCOPE_} 접두사를 붙여 표준 표기를 따른다.
   *
   * <p>여기서 사용자 권한을 그대로 싣더라도 에이전트가 관리자 행세를 하게 되지는 않는다. 무엇을 할 수 있는지는 결국 스코프가 좁히고, 등록된 에이전트 클라이언트가 받을
   * 수 있는 스코프에는 {@code posts:draft} 밖에 없기 때문이다.
   */
  private List<GrantedAuthority> authorities(Jwt jwt, AuthenticatedUser principal) {
    List<GrantedAuthority> authorities = new ArrayList<>(principal.getAuthorities());

    for (String scope : scopes(jwt)) {
      authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
    }

    return authorities;
  }

  private List<String> scopes(Jwt jwt) {
    Object claim = jwt.getClaim(SCOPE_CLAIM);
    if (claim instanceof String value) {
      return value.isBlank() ? List.of() : List.of(value.split(" "));
    }
    if (claim instanceof Collection<?> values) {
      return values.stream().map(String::valueOf).toList();
    }

    return List.of();
  }
}
