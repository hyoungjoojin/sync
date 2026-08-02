package com.skkil.sync.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.user.dto.request.ConfirmPasswordResetRequest;
import com.skkil.sync.user.dto.request.RequestPasswordResetRequest;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.service.PasswordResetService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = true)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class PasswordResetControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private PasswordResetService passwordResetService;

  @Test
  @DisplayName("[requestPasswordReset] API 문서화 테스트")
  void requestPasswordReset() throws Exception {
    RequestPasswordResetRequest request = new RequestPasswordResetRequest("user@example.com");

    mockMvc
        .perform(
            post("/auth/password-reset/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "RequestPasswordReset",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Request Password Reset")
                    .description(
                        "비밀번호 재설정 링크를 이메일로 발송합니다. 등록되지 않은 이메일이면 404와 함께 USER_NOT_FOUND를 반환합니다.")
                    .requestSchema(schema(RequestPasswordResetRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                requestFields(
                    fieldWithPath("email")
                        .type(JsonFieldType.STRING)
                        .description("재설정 링크를 받을 이메일"))));

    verify(passwordResetService).requestPasswordReset(eq(request));
  }

  @Test
  @DisplayName("[requestPasswordReset] 이메일 형식이 올바르지 않으면 400을 반환한다")
  void requestPasswordReset_invalidEmail_returnBadRequest() throws Exception {
    RequestPasswordResetRequest request = new RequestPasswordResetRequest("not-an-email");

    mockMvc
        .perform(
            post("/auth/password-reset/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[requestPasswordReset] 가입되지 않은 이메일이면 404를 반환한다")
  void requestPasswordReset_unknownEmail_returnNotFound() throws Exception {
    RequestPasswordResetRequest request = new RequestPasswordResetRequest("unknown@example.com");

    doThrow(new UserNotFoundException(request.email()))
        .when(passwordResetService)
        .requestPasswordReset(eq(request));

    mockMvc
        .perform(
            post("/auth/password-reset/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[confirmPasswordReset] API 문서화 테스트")
  void confirmPasswordReset() throws Exception {
    ConfirmPasswordResetRequest request =
        new ConfirmPasswordResetRequest("reset-token", "newPassword123");

    mockMvc
        .perform(
            post("/auth/password-reset/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "ConfirmPasswordReset",
                ResourceSnippetParameters.builder()
                    .tag("auth")
                    .summary("Confirm Password Reset")
                    .description("재설정 토큰을 사용해 새 비밀번호를 저장합니다. 토큰은 1회만 사용할 수 있으며, 기존 세션은 모두 만료됩니다.")
                    .requestSchema(schema(ConfirmPasswordResetRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                requestFields(
                    fieldWithPath("token").type(JsonFieldType.STRING).description("재설정 링크에 담긴 토큰"),
                    fieldWithPath("newPassword")
                        .type(JsonFieldType.STRING)
                        .description("새 비밀번호 (8-128자)"))));

    verify(passwordResetService).confirmPasswordReset(eq(request));
  }

  @Test
  @DisplayName("[confirmPasswordReset] 새 비밀번호가 너무 짧으면 400을 반환한다")
  void confirmPasswordReset_shortPassword_returnBadRequest() throws Exception {
    ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest("reset-token", "short");

    mockMvc
        .perform(
            post("/auth/password-reset/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
