package com.skkil.sync.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.skkil.sync.user.model.User;
import org.junit.jupiter.api.Test;

class UserMapperTests {

  private final UserMapper userMapper = new UserMapperImpl();

  @Test
  void toUserSummary_activeUser_isDeletedFalse() {
    User user = User.builder().email("user@example.com").fullName("User Name").build();
    user.setId(1L);

    var summary = userMapper.toUserSummary(user, "https://example.com/avatar.png");

    assertThat(summary.name()).isEqualTo("User Name");
    assertThat(summary.isDeleted()).isFalse();
  }

  @Test
  void toUserSummary_deletedUser_isDeletedTrue() {
    User user = User.builder().email("user@example.com").fullName("User Name").build();
    user.setId(1L);
    user.delete();

    var summary = userMapper.toUserSummary(user, null);

    assertThat(summary.isDeleted()).isTrue();
  }
}
