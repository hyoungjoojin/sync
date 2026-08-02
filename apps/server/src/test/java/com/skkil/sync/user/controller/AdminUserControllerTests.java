package com.skkil.sync.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.exception.UserNotFoundException;
import com.skkil.sync.user.service.UserService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class AdminUserControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Test
  @DisplayName("[promoteToAdmin] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void promoteToAdmin() throws Exception {
    String handle = "sync-user";

    doNothing().when(userService).promoteToAdmin(eq(handle));

    mockMvc
        .perform(
            patch("/admin/users/{handle}/promote", handle)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "PromoteUserToAdmin",
                ResourceSnippetParameters.builder()
                    .tag("user")
                    .summary("Promote User To Admin")
                    .description("주어진 핸들의 사용자를 플랫폼 ADMIN으로 승격합니다."),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("handle").description("승격할 사용자 핸들"))));
  }

  @Test
  @DisplayName("[promoteToAdmin] 존재하지 않는 핸들이면 404를 반환한다")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void promoteToAdminUserNotFound() throws Exception {
    String handle = "not-exist";

    doThrow(new UserNotFoundException(handle)).when(userService).promoteToAdmin(eq(handle));

    mockMvc
        .perform(
            patch("/admin/users/{handle}/promote", handle)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()));
  }
}
