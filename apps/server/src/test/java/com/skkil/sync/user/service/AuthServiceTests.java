package com.skkil.sync.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.auth.session.SessionInvalidationService;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.dto.request.ChangePasswordRequest;
import com.skkil.sync.user.dto.request.LoginRequest;
import com.skkil.sync.user.dto.request.RegisterRequest;
import com.skkil.sync.user.exception.InvalidCurrentPasswordException;
import com.skkil.sync.user.exception.UserAlreadyExistsException;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.PasswordResetTokenRepository;
import com.skkil.sync.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

  @Mock private UserService userService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private SessionInvalidationService sessionInvalidationService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("authenticate 시 유효한 사용자는 인증 성공")
  void authenticate_validUser_success() {
    LoginRequest request = new LoginRequest("user@example.com", "password123");
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(
            1L, "Test User", "user@example.com", "hashedPassword", Role.USER, true);

    when(userService.loadUserByUsername("user@example.com")).thenReturn(authenticatedUser);
    Authentication mockAuth = mock(Authentication.class);
    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuth);

    Authentication result = authService.authenticate(request);

    assertThat(result).isNotNull();
    verify(userService).loadUserByUsername("user@example.com");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("authenticate 시 사용자가 존재하지 않으면 UsernameNotFoundException 발생")
  void authenticate_userDoesNotExist_throwUsernameNotFoundException() {
    LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

    when(userService.loadUserByUsername("nonexistent@example.com"))
        .thenThrow(new UsernameNotFoundException("User not found"));

    assertThatThrownBy(() -> authService.authenticate(request))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(authenticationManager, never()).authenticate(any(Authentication.class));
  }

  @Test
  @DisplayName("authenticate 시 비밀번호가 설정되지 않은 OAuth 사용자는 IllegalArgumentException 발생")
  void authenticate_oAuthUserWithNoPassword_throwIllegalArgumentException() {
    LoginRequest request = new LoginRequest("oauth@example.com", "password123");
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(1L, "OAuth User", "oauth@example.com", null, Role.USER, true);

    when(userService.loadUserByUsername("oauth@example.com")).thenReturn(authenticatedUser);

    assertThatThrownBy(() -> authService.authenticate(request))
        .isInstanceOf(BadCredentialsException.class);

    verify(authenticationManager, never()).authenticate(any(Authentication.class));
  }

  @Test
  @DisplayName("registerUser 시 새 사용자는 등록 성공")
  void registerUser_newUser_success() {
    RegisterRequest request =
        new RegisterRequest("newuser@example.com", "password123", "captcha-token");
    String encodedPassword = "encodedPassword";

    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });

    authService.registerUser(request);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser)
        .hasFieldOrPropertyWithValue("email", "newuser@example.com")
        .hasFieldOrPropertyWithValue("hashedPassword", encodedPassword)
        .hasFieldOrPropertyWithValue("fullName", "");

    verify(passwordEncoder).encode("password123");

    verify(eventPublisher).publishEvent(any());
  }

  @Test
  @DisplayName("registerUser 시 이미 존재하는 이메일은 UserAlreadyExistsException 발생")
  void registerUser_existingEmail_throwUserAlreadyExistsException() {
    RegisterRequest request =
        new RegisterRequest("existing@example.com", "password123", "captcha-token");
    User existingUser =
        User.builder()
            .email("existing@example.com")
            .fullName("Existing User")
            .hashedPassword("hashedPassword")
            .build();

    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> authService.registerUser(request))
        .isInstanceOf(UserAlreadyExistsException.class);

    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("registerUser 시 비밀번호는 암호화되어 저장")
  void registerUser_passwordIsEncoded() {
    RegisterRequest request =
        new RegisterRequest("user@example.com", "plainPassword", "captcha-token");
    String encodedPassword = "encodedHashedPassword";

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });

    authService.registerUser(request);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getHashedPassword()).isEqualTo(encodedPassword);
  }

  @Test
  @DisplayName("changePassword 시 현재 비밀번호가 일치하면 갱신되고 모든 세션이 무효화된다")
  void changePassword_validCurrentPassword_success() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("old").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("currentPassword", "old")).thenReturn(true);
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNew");

    authService.changePassword(1L, new ChangePasswordRequest("currentPassword", "newPassword123"));

    assertThat(user.getHashedPassword()).isEqualTo("encodedNew");
    verify(userRepository).save(user);
    verify(passwordResetTokenRepository).deleteByUser(user);
    verify(sessionInvalidationService).invalidateAllSessions("user@example.com");
  }

  @Test
  @DisplayName("changePassword 시 현재 비밀번호가 틀리면 InvalidCurrentPasswordException 발생")
  void changePassword_wrongCurrentPassword_throwException() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("old").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "old")).thenReturn(false);

    assertThatThrownBy(
            () -> authService.changePassword(1L, new ChangePasswordRequest("wrong", "newPass123")))
        .isInstanceOf(InvalidCurrentPasswordException.class);

    verify(userRepository, never()).save(any(User.class));
    verify(sessionInvalidationService, never()).invalidateAllSessions(anyString());
  }

  @Test
  @DisplayName("changePassword 시 비밀번호가 있는 사용자가 현재 비밀번호를 생략하면 InvalidCurrentPasswordException 발생")
  void changePassword_missingCurrentPassword_throwException() {
    User user =
        User.builder().email("user@example.com").fullName("User").hashedPassword("old").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThatThrownBy(
            () -> authService.changePassword(1L, new ChangePasswordRequest(null, "newPass123")))
        .isInstanceOf(InvalidCurrentPasswordException.class);

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("changePassword 시 OAuth2 전용 사용자는 현재 비밀번호 없이 최초 비밀번호를 설정할 수 있다")
  void changePassword_oAuthOnlyUser_setsFirstPassword() {
    User user =
        User.builder().email("oauth@example.com").fullName("User").hashedPassword(null).build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNew");

    authService.changePassword(1L, new ChangePasswordRequest(null, "newPassword123"));

    assertThat(user.getHashedPassword()).isEqualTo("encodedNew");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(userRepository).save(user);
    verify(sessionInvalidationService).invalidateAllSessions("oauth@example.com");
  }
}
