package com.skkil.sync.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.auth.session.SessionInvalidationService;
import com.skkil.sync.common.integration.email.EmailService;
import com.skkil.sync.common.integration.email.dto.EmailMessage;
import com.skkil.sync.user.dto.request.ConfirmPasswordResetRequest;
import com.skkil.sync.user.dto.request.RequestPasswordResetRequest;
import com.skkil.sync.user.exception.PasswordResetTokenExpiredException;
import com.skkil.sync.user.exception.PasswordResetTokenInvalidException;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.model.EmailVerificationToken;
import com.skkil.sync.user.model.PasswordResetToken;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.EmailVerificationTokenRepository;
import com.skkil.sync.user.repository.PasswordResetTokenRepository;
import com.skkil.sync.user.repository.UserRepository;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetServiceTests {

  private static final String FRONTEND_BASE_URL = "https://sync.example.com";

  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository tokenRepository;
  @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
  @Mock private SessionInvalidationService sessionInvalidationService;
  @Mock private EmailService emailService;
  @Mock private SpringTemplateEngine templateEngine;
  @Mock private PasswordEncoder passwordEncoder;

  private PasswordResetService passwordResetService;

  @BeforeEach
  void setUp() throws Exception {
    when(emailService.sendMessage(any(EmailMessage.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

    passwordResetService =
        new PasswordResetService(
            userRepository,
            tokenRepository,
            emailVerificationTokenRepository,
            sessionInvalidationService,
            emailService,
            templateEngine,
            passwordEncoder,
            FRONTEND_BASE_URL);
  }

  @Test
  @DisplayName("requestPasswordReset 시 존재하지 않는 이메일이면 예외를 던지고 아무것도 발송하지 않는다")
  void requestPasswordReset_unknownEmail_throwNotFoundException() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                passwordResetService.requestPasswordReset(
                    new RequestPasswordResetRequest("unknown@example.com")))
        .isInstanceOf(UserNotFoundException.class);

    verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    verify(emailService, never()).sendMessage(any(EmailMessage.class));
  }

  @Test
  @DisplayName("requestPasswordReset 시 탈퇴한 사용자는 존재하지 않는 것으로 취급한다")
  void requestPasswordReset_deletedUser_throwNotFoundException() {
    User user = mock(User.class);
    when(user.getDeletedAt()).thenReturn(java.time.Instant.now());
    when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.of(user));

    assertThatThrownBy(
            () ->
                passwordResetService.requestPasswordReset(
                    new RequestPasswordResetRequest("deleted@example.com")))
        .isInstanceOf(UserNotFoundException.class);

    verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    verify(emailService, never()).sendMessage(any(EmailMessage.class));
  }

  @Test
  @DisplayName("requestPasswordReset 시 메일에 담긴 원본 토큰이 아니라 해시가 저장된다")
  void requestPasswordReset_storesHashOfEmailedToken() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("hashed").build();
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());

    passwordResetService.requestPasswordReset(new RequestPasswordResetRequest("user@example.com"));

    ArgumentCaptor<PasswordResetToken> tokenCaptor =
        ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(tokenRepository).save(tokenCaptor.capture());

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(templateEngine).process(anyString(), contextCaptor.capture());

    String resetUrl = (String) contextCaptor.getValue().getVariable("resetUrl");
    assertThat(resetUrl).startsWith(FRONTEND_BASE_URL + "/auth/reset-password?token=");

    String rawToken =
        URLDecoder.decode(
            resetUrl.substring(resetUrl.indexOf("token=") + 6), StandardCharsets.UTF_8);
    assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo(sha256(rawToken)).hasSize(64);
    assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(rawToken);

    ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailService).sendMessage(emailCaptor.capture());
    assertThat(emailCaptor.getValue().to()).isEqualTo("user@example.com");
  }

  @Test
  @DisplayName("requestPasswordReset 시 재발송 쿨다운 중이면 발송하지 않는다")
  void requestPasswordReset_withinCooldown_doesNotResend() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("hashed").build();
    PasswordResetToken existing = mock(PasswordResetToken.class);
    when(existing.isWithinResendCooldown()).thenReturn(true);

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(tokenRepository.findByUser(user)).thenReturn(Optional.of(existing));

    passwordResetService.requestPasswordReset(new RequestPasswordResetRequest("user@example.com"));

    verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    verify(emailService, never()).sendMessage(any(EmailMessage.class));
  }

  @Test
  @DisplayName("confirmPasswordReset 시 존재하지 않는 토큰이면 PasswordResetTokenInvalidException 발생")
  void confirmPasswordReset_unknownToken_throwInvalidException() {
    when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                passwordResetService.confirmPasswordReset(
                    new ConfirmPasswordResetRequest("nope", "newPassword123")))
        .isInstanceOf(PasswordResetTokenInvalidException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("confirmPasswordReset 시 만료된 토큰이면 PasswordResetTokenExpiredException 발생")
  void confirmPasswordReset_expiredToken_throwExpiredException() {
    PasswordResetToken token = mock(PasswordResetToken.class);
    when(token.isExpired()).thenReturn(true);
    when(tokenRepository.findByTokenHash(sha256("raw-token"))).thenReturn(Optional.of(token));

    assertThatThrownBy(
            () ->
                passwordResetService.confirmPasswordReset(
                    new ConfirmPasswordResetRequest("raw-token", "newPassword123")))
        .isInstanceOf(PasswordResetTokenExpiredException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("confirmPasswordReset 시 비밀번호가 갱신되고 토큰 삭제와 세션 무효화가 수행된다")
  void confirmPasswordReset_success() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("old").build();
    PasswordResetToken token = mock(PasswordResetToken.class);
    EmailVerificationToken verificationToken = mock(EmailVerificationToken.class);

    when(token.isExpired()).thenReturn(false);
    when(token.getUser()).thenReturn(user);
    when(tokenRepository.findByTokenHash(sha256("raw-token"))).thenReturn(Optional.of(token));
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNew");
    when(emailVerificationTokenRepository.findByUser(user))
        .thenReturn(Optional.of(verificationToken));

    passwordResetService.confirmPasswordReset(
        new ConfirmPasswordResetRequest("raw-token", "newPassword123"));

    assertThat(user.getHashedPassword()).isEqualTo("encodedNew");
    assertThat(user.isVerified()).isTrue();
    verify(emailVerificationTokenRepository).delete(verificationToken);
    verify(userRepository).save(user);
    verify(tokenRepository).delete(token);
    verify(sessionInvalidationService).invalidateAllSessions("user@example.com");
  }

  @Test
  @DisplayName("confirmPasswordReset 시 이미 인증된 사용자는 인증 토큰을 건드리지 않는다")
  void confirmPasswordReset_alreadyVerifiedUser_leavesVerificationTokenAlone() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("old").build();
    user.verifyEmail();

    PasswordResetToken token = mock(PasswordResetToken.class);
    when(token.isExpired()).thenReturn(false);
    when(token.getUser()).thenReturn(user);
    when(tokenRepository.findByTokenHash(sha256("raw-token"))).thenReturn(Optional.of(token));
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNew");

    passwordResetService.confirmPasswordReset(
        new ConfirmPasswordResetRequest("raw-token", "newPassword123"));

    verify(emailVerificationTokenRepository, never()).delete(any(EmailVerificationToken.class));
    assertThat(user.isVerified()).isTrue();
  }

  private static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
