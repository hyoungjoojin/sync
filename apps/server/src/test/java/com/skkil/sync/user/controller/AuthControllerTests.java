package com.skkil.sync.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.integration.captcha.CaptchaService;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.dto.request.ChangePasswordRequest;
import com.skkil.sync.user.dto.request.LoginRequest;
import com.skkil.sync.user.dto.request.RegisterRequest;
import com.skkil.sync.user.service.AuthService;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class AuthControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private AuthService authService;

  @MockitoBean private CaptchaService captchaService;

  @Test
  @DisplayName("[login] API 문서화 테스트")
  void login() throws Exception {
    LoginRequest request = new LoginRequest("user@example.com", "password123");

    AuthenticatedUser user =
        new AuthenticatedUser(1L, "user", "user@example.com", "hashedPassword", Role.USER, true);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, request.password(), user.getAuthorities());

    when(authService.authenticate(any(LoginRequest.class))).thenReturn(authentication);

    mockMvc
        .perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "Login",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Login")
                    .description("Login")
                    .requestSchema(schema(LoginRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING).description("Email"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("Password"))));
  }

  @Test
  @DisplayName("[logout] API 문서화 테스트")
  @WithAuthenticatedUser
  void logout() throws Exception {
    mockMvc
        .perform(post("/auth/logout").with(csrf()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "Logout",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Logout")
                    .description("Logout"),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity()));
  }

  @Test
  @DisplayName("[register] API 문서화 테스트")
  void register() throws Exception {
    RegisterRequest request =
        new RegisterRequest("newuser@example.com", "password123", "captcha-token");

    mockMvc
        .perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "Register",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Register")
                    .description("Register")
                    .requestSchema(schema(RegisterRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING).description("Email"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("Password"),
                    fieldWithPath("captchaToken")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("캡차 검증에 사용되는 토큰. 서버에 시크릿 키가 설정되지 않은 환경에서는 생략할 수 있습니다."))));

    verify(captchaService).verify("captcha-token", "register");
  }

  @Test
  void register_passwordIsBad_returnBadRequest() throws Exception {
    RegisterRequest request = new RegisterRequest("user@example.com", "short", "captcha-token");

    mockMvc
        .perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[changePassword] API 문서화 테스트")
  @WithAuthenticatedUser(id = 1L)
  void changePassword() throws Exception {
    ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword123");

    mockMvc
        .perform(
            patch("/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "ChangePassword",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Change Password")
                    .description(
                        "로그인한 사용자의 비밀번호를 변경합니다. 변경하면 기존 세션이 모두 만료되고 새 세션이 발급되며, 발급된 재설정 링크도 무효화됩니다.")
                    .requestSchema(schema(ChangePasswordRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                requestFields(
                    fieldWithPath("currentPassword")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("현재 비밀번호. OAuth2 로만 가입해 비밀번호가 없는 사용자는 생략할 수 있습니다."),
                    fieldWithPath("newPassword")
                        .type(JsonFieldType.STRING)
                        .description("새 비밀번호 (8-128자)"))));

    verify(authService).changePassword(eq(1L), eq(request));
  }

  @Test
  @DisplayName("[changePassword] 로그인하지 않은 사용자는 접근할 수 없다")
  void changePassword_unauthenticatedUser_shouldReturnUnauthorized() throws Exception {
    ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword123");

    mockMvc
        .perform(
            patch("/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("[changePassword] 새 비밀번호가 너무 짧으면 400을 반환한다")
  @WithAuthenticatedUser(id = 1L)
  void changePassword_shortNewPassword_returnBadRequest() throws Exception {
    ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "short");

    mockMvc
        .perform(
            patch("/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
