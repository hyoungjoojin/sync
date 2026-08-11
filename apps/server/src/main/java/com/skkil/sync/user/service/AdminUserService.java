package com.skkil.sync.user.service;

import com.skkil.sync.auth.session.SessionInvalidationService;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.dto.summary.AdminUserSummary;
import com.skkil.sync.user.exception.UserCannotBeDeletedException;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.mapper.AdminUserAssembler;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

  private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

  private final UserRepository userRepository;

  private final AdminUserAssembler adminUserAssembler;

  private final SessionInvalidationService sessionInvalidationService;

  public AdminUserService(
      UserRepository userRepository,
      AdminUserAssembler adminUserAssembler,
      SessionInvalidationService sessionInvalidationService) {
    this.userRepository = userRepository;
    this.adminUserAssembler = adminUserAssembler;
    this.sessionInvalidationService = sessionInvalidationService;
  }

  @Transactional(readOnly = true)
  public AdminUserSummary searchUser(String query) {
    User user =
        userRepository
            .searchUserForAdmin(query)
            .orElseThrow(() -> new UserNotFoundException(query));

    return adminUserAssembler.toAdminUserSummary(user);
  }

  @Transactional
  public void deleteUser(Long requesterId, String handle) {
    User user =
        userRepository.findByHandle(handle).orElseThrow(() -> new UserNotFoundException(handle));

    if (user.getId().equals(requesterId)) {
      throw new UserCannotBeDeletedException("Administrators cannot delete their own account.");
    }

    if (user.getRole() == Role.ADMIN) {
      throw new UserCannotBeDeletedException("Administrator accounts cannot be deleted.");
    }

    if (user.getDeletedAt() != null) {
      throw new UserCannotBeDeletedException("User is already deleted.");
    }

    String email = user.getEmail();
    user.delete();
    userRepository.save(user);
    sessionInvalidationService.invalidateAllSessions(email);

    log.warn("관리자가 사용자를 삭제했습니다. id={}, handle={}", user.getId(), handle);
  }
}
