package com.skkil.sync.auth.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 에이전트에게 발급된 OAuth2 인가 기록을 지운다.
 *
 * <p>액세스 토큰(1시간)과 갱신 토큰(30일)은 세션이 아니라 {@code oauth2_authorization} 에 산다. 비밀번호를 바꿔도 이것들이 남아 있으면 훔친
 * 갱신 토큰으로 한 달 동안 계속 새 액세스 토큰을 받을 수 있으므로, 세션을 지우는 자리에서 함께 지운다.
 *
 * <p>{@code app.agent.enabled} 와 무관하게 항상 등록한다 — 기능을 껐다고 해서 이미 발급된 토큰이 사라지지는 않기 때문이다.
 */
@Component
@Slf4j
public class AgentAuthorizationRevoker {

  private final JdbcTemplate jdbcTemplate;

  public AgentAuthorizationRevoker(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void revokeAll(String principalName) {
    int revoked =
        jdbcTemplate.update(
            "DELETE FROM oauth2_authorization WHERE principal_name = ?", principalName);

    if (revoked > 0) {
      log.info("Revoked {} agent authorization(s)", revoked);
    }
  }
}
