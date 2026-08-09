package com.skkil.sync.common.seeder;

import com.skkil.sync.user.constant.Handle;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@Slf4j
class AdminSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final UserSeeder userSeeder;
  private final String email;
  private final String password;
  private final String handle;
  private final String name;

  AdminSeeder(
      UserRepository userRepository,
      UserSeeder userSeeder,
      @Value("${app.admin.email:}") String email,
      @Value("${app.admin.password:}") String password,
      @Value("${app.admin.handle:sync-admin}") String handle,
      @Value("${app.admin.name:관리자}") String name) {
    this.userRepository = userRepository;
    this.userSeeder = userSeeder;
    this.email = email;
    this.password = password;
    this.handle = handle;
    this.name = name;
  }

  @Override
  public void run(ApplicationArguments args) {
    seed();
  }

  private Optional<User> seed() {
    if (email.isBlank()) {
      log.info("app.admin.email이 설정되지 않아 ADMIN 계정 시딩을 건너뜁니다");
      return Optional.empty();
    }

    return Optional.ofNullable(
        userRepository.findByEmail(email).map(this::promote).orElseGet(this::create));
  }

  private User promote(User user) {
    if (user.getRole() == Role.ADMIN) {
      log.info("ADMIN 계정이 이미 존재하여 시딩을 건너뜁니다");
      return user;
    }

    user.setRole(Role.ADMIN);
    log.warn("기존 사용자를 플랫폼 ADMIN으로 승격했습니다. id={}", user.getId());
    return userRepository.save(user);
  }

  private User create() {
    if (password.isBlank()) {
      log.warn("app.admin.email이 설정되었으나 해당 계정이 없고 app.admin.password도 비어 있어 ADMIN 계정을 생성하지 않았습니다");
      return null;
    }

    if (handle.length() < Handle.MIN_LENGTH || handle.length() > Handle.MAX_LENGTH) {
      throw new IllegalStateException(
          "app.admin.handle은 %d자 이상 %d자 이하여야 합니다. 현재 값: %s"
              .formatted(Handle.MIN_LENGTH, Handle.MAX_LENGTH, handle));
    }

    User user = userSeeder.seed(email, password, handle, name, "", "", Role.ADMIN);
    log.warn("플랫폼 ADMIN 계정을 생성했습니다. id={}, handle={}", user.getId(), handle);
    return user;
  }
}
