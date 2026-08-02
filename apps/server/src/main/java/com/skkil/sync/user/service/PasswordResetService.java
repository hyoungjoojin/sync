package com.skkil.sync.user.service;

import com.skkil.sync.auth.session.SessionInvalidationService;
import com.skkil.sync.common.integration.email.EmailService;
import com.skkil.sync.common.integration.email.dto.EmailMessage;
import com.skkil.sync.user.constant.PasswordResetConstants;
import com.skkil.sync.user.dto.request.ConfirmPasswordResetRequest;
import com.skkil.sync.user.dto.request.RequestPasswordResetRequest;
import com.skkil.sync.user.exception.PasswordResetTokenExpiredException;
import com.skkil.sync.user.exception.PasswordResetTokenInvalidException;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.model.PasswordResetToken;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.EmailVerificationTokenRepository;
import com.skkil.sync.user.repository.PasswordResetTokenRepository;
import com.skkil.sync.user.repository.UserRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final SessionInvalidationService sessionInvalidationService;
  private final EmailService emailService;
  private final SpringTemplateEngine templateEngine;
  private final PasswordEncoder passwordEncoder;
  private final Random random;
  private final String frontendBaseUrl;

  public PasswordResetService(
      UserRepository userRepository,
      PasswordResetTokenRepository tokenRepository,
      EmailVerificationTokenRepository emailVerificationTokenRepository,
      SessionInvalidationService sessionInvalidationService,
      EmailService emailService,
      SpringTemplateEngine templateEngine,
      PasswordEncoder passwordEncoder,
      @Value("${app.frontend.base-url}") String frontendBaseUrl)
      throws NoSuchAlgorithmException {
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    this.sessionInvalidationService = sessionInvalidationService;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.passwordEncoder = passwordEncoder;
    this.random = SecureRandom.getInstanceStrong();
    this.frontendBaseUrl = frontendBaseUrl;
  }

  @Transactional
  public void requestPasswordReset(RequestPasswordResetRequest request) {
    User user =
        userRepository
            .findByEmail(request.email())
            .filter(found -> found.getDeletedAt() == null)
            .orElseThrow(() -> new UserNotFoundException(request.email()));

    Optional<PasswordResetToken> existing = tokenRepository.findByUser(user);
    if (existing.map(PasswordResetToken::isWithinResendCooldown).orElse(false)) {
      log.debug("Password reset for user {} is within the resend cooldown", user.getId());
      return;
    }

    String token = generateToken();
    PasswordResetToken resetToken =
        existing
            .map(
                found -> {
                  found.refresh(hashToken(token));
                  return found;
                })
            .orElseGet(
                () -> PasswordResetToken.builder().user(user).tokenHash(hashToken(token)).build());

    tokenRepository.save(resetToken);

    sendPasswordResetEmail(user, token);
  }

  @Transactional
  public void confirmPasswordReset(ConfirmPasswordResetRequest request) {
    PasswordResetToken resetToken = resolveToken(request.token());
    User user = resetToken.getUser();

    user.setHashedPassword(passwordEncoder.encode(request.newPassword()));

    if (!user.isVerified()) {
      user.verifyEmail();
      emailVerificationTokenRepository
          .findByUser(user)
          .ifPresent(emailVerificationTokenRepository::delete);
    }

    userRepository.save(user);
    tokenRepository.delete(resetToken);

    sessionInvalidationService.invalidateAllSessions(user.getEmail());
    log.info("Password reset completed for user {}", user.getId());
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @Transactional
  public void removeExpiredTokens() {
    int removed = tokenRepository.deleteByExpiresAtBefore(Instant.now());
    if (removed > 0) {
      log.debug("Removed {} expired password reset tokens", removed);
    }
  }

  private PasswordResetToken resolveToken(String token) {
    PasswordResetToken resetToken =
        tokenRepository
            .findByTokenHash(hashToken(token))
            .orElseThrow(PasswordResetTokenInvalidException::new);

    if (resetToken.isExpired()) {
      throw new PasswordResetTokenExpiredException();
    }

    if (resetToken.getUser().getDeletedAt() != null) {
      throw new PasswordResetTokenInvalidException();
    }

    return resetToken;
  }

  private void sendPasswordResetEmail(User user, String token) {
    Context context = new Context();
    context.setVariable("resetUrl", buildResetUrl(token));
    context.setVariable(
        "expirationMinutes", PasswordResetConstants.PASSWORD_RESET_TOKEN_TTL.toMinutes());

    EmailMessage email =
        EmailMessage.builder()
            .to(user.getEmail())
            .subject("sync 비밀번호 재설정")
            .text(templateEngine.process("email/reset-password", context))
            .build();

    log.debug("Sending password reset email to user {}", user.getId());
    emailService
        .sendMessage(email)
        .exceptionally(
            e -> {
              log.error("Failed to send password reset email to user {}", user.getId(), e);
              return null;
            });
  }

  private String buildResetUrl(String token) {
    return frontendBaseUrl
        + "/auth/reset-password?token="
        + URLEncoder.encode(token, StandardCharsets.UTF_8);
  }

  private String generateToken() {
    byte[] bytes = new byte[PasswordResetConstants.PASSWORD_RESET_TOKEN_BYTE_LENGTH];
    random.nextBytes(bytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
