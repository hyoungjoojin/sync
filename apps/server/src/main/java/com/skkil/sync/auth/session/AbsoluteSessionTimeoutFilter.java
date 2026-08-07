package com.skkil.sync.auth.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class AbsoluteSessionTimeoutFilter extends OncePerRequestFilter {

  private final Duration absoluteTimeout;

  public AbsoluteSessionTimeoutFilter(
      @Value("${app.session.absolute-timeout}") Duration absoluteTimeout) {
    this.absoluteTimeout = absoluteTimeout;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Instant createdAt = Instant.ofEpochMilli(session.getCreationTime());
      if (Instant.now().isAfter(createdAt.plus(absoluteTimeout))) {
        log.info("Invalidating session {} past absolute timeout", session.getId());
        session.invalidate();
      }
    }

    chain.doFilter(request, response);
  }
}
