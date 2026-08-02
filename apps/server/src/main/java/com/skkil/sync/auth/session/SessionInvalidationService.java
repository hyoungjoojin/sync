package com.skkil.sync.auth.session;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SessionInvalidationService {

  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

  public SessionInvalidationService(
      FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  public void invalidateAllSessions(String principalName) {
    Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(principalName);
    sessions.keySet().forEach(sessionRepository::deleteById);

    log.info("Invalidated {} session(s)", sessions.size());
  }
}
