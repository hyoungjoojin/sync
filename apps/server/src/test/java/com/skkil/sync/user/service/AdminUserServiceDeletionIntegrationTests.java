package com.skkil.sync.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skkil.sync.common.config.TestcontainersConfig;
import com.skkil.sync.user.dto.request.LoginRequest;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = "app.seed.enabled=false")
class AdminUserServiceDeletionIntegrationTests {

  private static final String SESSION_COOKIE = "SESSION";
  private static final String PASSWORD = "password1234";

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FindByIndexNameSessionRepository<? extends Session> sessionRepository;
  @Autowired private AdminUserService adminUserService;

  private String email;
  private String handle;
  private Long userId;

  @BeforeEach
  void setUp() {
    long unique = System.nanoTime();
    email = "delete-test-" + unique + "@example.com";
    handle = "delete-test-" + unique;

    User user =
        User.builder()
            .email(email)
            .fullName("Delete Test")
            .hashedPassword(passwordEncoder.encode(PASSWORD))
            .build();
    user.updateFields(null, null, "Original bio");
    user = userRepository.save(user);
    user.updateHandle(handle);
    user = userRepository.save(user);

    userId = user.getId();
  }

  @Test
  @DisplayName("사용자 삭제 시 개인정보가 익명화되고 이메일/핸들을 재사용할 수 있다")
  void deleteUser_anonymizesPersonalInformationAndFreesUpEmailAndHandle() {
    adminUserService.deleteUser(-1L, handle);

    User deleted = userRepository.findById(userId).orElseThrow();
    assertThat(deleted.getDeletedAt()).isNotNull();
    assertThat(deleted.getEmail()).isNotEqualTo(email);
    assertThat(deleted.getHandle()).isNotEqualTo(handle);
    assertThat(deleted.getFullName()).isNotEqualTo("Delete Test");
    assertThat(deleted.getBio()).isEmpty();
    assertThat(deleted.getHashedPassword()).isNull();

    assertThat(userRepository.findByEmail(email)).isEmpty();
    assertThat(userRepository.findByHandle(handle)).isEmpty();

    User reclaimed =
        userRepository.save(
            User.builder().email(email).fullName("New Owner").hashedPassword("hash").build());
    reclaimed.updateHandle(handle);
    userRepository.save(reclaimed);

    assertThat(userRepository.findByEmail(email)).isPresent();
    assertThat(userRepository.findByHandle(handle)).isPresent();
  }

  @Test
  @DisplayName("사용자 삭제 시 기존 세션이 모두 무효화되고 재로그인이 차단된다")
  void deleteUser_invalidatesSessionsAndBlocksFutureLogin() throws Exception {
    Cookie session = login();
    assertThat(sessionRepository.findByPrincipalName(email)).hasSize(1);

    adminUserService.deleteUser(-1L, handle);

    assertThat(sessionRepository.findByPrincipalName(email)).isEmpty();
    assertThat(statusOf(session)).isEqualTo(HttpStatus.UNAUTHORIZED.value());

    mockMvc
        .perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
        .andExpect(status().isUnauthorized());
  }

  private Cookie login() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
            .andExpect(status().isNoContent())
            .andReturn();

    Cookie cookie = result.getResponse().getCookie(SESSION_COOKIE);
    assertThat(cookie).isNotNull();

    return cookie;
  }

  private int statusOf(Cookie cookie) throws Exception {
    return mockMvc
        .perform(get("/profiles/me").cookie(cookie))
        .andReturn()
        .getResponse()
        .getStatus();
  }
}
