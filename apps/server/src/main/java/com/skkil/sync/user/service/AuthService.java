package com.skkil.sync.user.service;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.auth.session.SessionInvalidationService;
import com.skkil.sync.user.dto.request.ChangePasswordRequest;
import com.skkil.sync.user.dto.request.LoginRequest;
import com.skkil.sync.user.dto.request.RegisterRequest;
import com.skkil.sync.user.event.UserRegisteredEvent;
import com.skkil.sync.user.exception.InvalidCurrentPasswordException;
import com.skkil.sync.user.exception.UserAlreadyExistsException;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.PasswordResetTokenRepository;
import com.skkil.sync.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

  private final UserService userService;
  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final SessionInvalidationService sessionInvalidationService;
  private final ApplicationEventPublisher eventPublisher;

  public AuthService(
      UserService userService,
      UserRepository userRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      SessionInvalidationService sessionInvalidationService,
      ApplicationEventPublisher eventPublisher) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.sessionInvalidationService = sessionInvalidationService;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true)
  public Authentication authenticate(LoginRequest request) {
    AuthenticatedUser principal = userService.loadUserByUsername(request.email());
    if (principal.password() == null) {
      log.debug("Authentication failed, user has no password set");
      throw new BadCredentialsException("");
    }

    Authentication authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            principal, request.password(), principal.getAuthorities());
    authenticationManager.authenticate(authentication);

    return authentication;
  }

  @Transactional
  public User registerUser(RegisterRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new UserAlreadyExistsException(request.email());
    }

    User user =
        User.builder()
            .fullName("")
            .email(request.email())
            .hashedPassword(passwordEncoder.encode(request.password()))
            .build();

    user = userRepository.save(user);
    log.info("Registered new user with id {}", user.getId());

    eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));

    return user;
  }

  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getHashedPassword() != null
        && (request.currentPassword() == null
            || !passwordEncoder.matches(request.currentPassword(), user.getHashedPassword()))) {
      throw new InvalidCurrentPasswordException();
    }

    user.setHashedPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    passwordResetTokenRepository.deleteByUser(user);

    sessionInvalidationService.invalidateAllSessions(user.getEmail());
    log.info("Password changed for user {}", userId);
  }
}
