package com.skkil.sync.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("[promoteToAdmin] 핸들에 해당하는 사용자를 ADMIN으로 승격한다")
  void promoteToAdmin_promotesUserToAdmin() {
    String handle = "sync-user";
    User user = new User(1L);

    when(userRepository.findByHandle(handle)).thenReturn(Optional.of(user));

    userService.promoteToAdmin(handle);

    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  @DisplayName("[promoteToAdmin] 핸들에 해당하는 사용자가 없으면 UserNotFoundException 예외 발생")
  void promoteToAdmin_userNotFound_throwUserNotFound() {
    String handle = "not-exist";

    when(userRepository.findByHandle(handle)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.promoteToAdmin(handle))
        .isInstanceOf(UserNotFoundException.class);
  }
}
