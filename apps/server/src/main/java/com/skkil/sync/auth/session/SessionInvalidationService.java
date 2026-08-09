package com.skkil.sync.auth.session;

import com.skkil.sync.auth.agent.AgentAuthorizationRevoker;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

/**
 * 비밀번호가 바뀌었을 때 그 계정으로 발급된 자격 증명을 전부 끊는다.
 *
 * <p>세션만으로는 부족하다. 에이전트 OAuth2 인가는 세션 저장소가 아니라 {@code oauth2_authorization} 에 남고, 갱신 토큰은 60일을 산다. 둘을
 * 한자리에서 지워야 "비밀번호를 바꾸면 모든 자격 증명이 무효가 된다"는 규칙이 실제로 성립한다.
 */
@Service
@Slf4j
public class SessionInvalidationService {

  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
  private final AgentAuthorizationRevoker agentAuthorizationRevoker;

  public SessionInvalidationService(
      FindByIndexNameSessionRepository<? extends Session> sessionRepository,
      AgentAuthorizationRevoker agentAuthorizationRevoker) {
    this.sessionRepository = sessionRepository;
    this.agentAuthorizationRevoker = agentAuthorizationRevoker;
  }

  public void invalidateAllSessions(String principalName) {
    Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(principalName);
    sessions.keySet().forEach(sessionRepository::deleteById);

    log.info("Invalidated {} session(s)", sessions.size());

    agentAuthorizationRevoker.revokeAll(principalName);
  }
}
