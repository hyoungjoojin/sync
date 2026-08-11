package com.skkil.sync.user.service;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.dto.response.GetHandleAvailabilityResponse;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    return new AuthenticatedUser(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getHashedPassword(),
        user.getRole(),
        user.getDeletedAt() == null);
  }

  public User getUserReference(Long userId) {
    return userRepository.getReferenceById(userId);
  }

  @Transactional(readOnly = true)
  public GetHandleAvailabilityResponse getHandleAvailability(Long requestingUserId, String handle) {
    boolean available =
        userRepository
            .findByHandle(handle)
            .map(user -> user.getId().equals(requestingUserId))
            .orElse(true);

    return new GetHandleAvailabilityResponse(available);
  }

  @Transactional
  public void promoteToAdmin(String handle) {
    User user =
        userRepository.findByHandle(handle).orElseThrow(() -> new UserNotFoundException(handle));

    user.setRole(Role.ADMIN);
    log.warn("기존 사용자를 플랫폼 ADMIN으로 승격했습니다. id={}", user.getId());
  }
}
