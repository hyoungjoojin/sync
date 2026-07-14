package com.skkil.sync.notification.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.security.WithAuthenticatedUserSecurityContextFactory;
import com.skkil.sync.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.skkil.sync.notification.dto.response.GetNotificationPreferencesResponse;
import com.skkil.sync.notification.dto.response.GetNotificationsResponse;
import com.skkil.sync.notification.service.NotificationPreferencesService;
import com.skkil.sync.notification.service.NotificationService;
import com.skkil.sync.notification.snippets.GetNotificationPreferencesResponseSnippets;
import com.skkil.sync.notification.snippets.GetNotificationsResponseSnippets;
import com.skkil.sync.notification.snippets.UpdateNotificationPreferencesRequestSnippets;
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
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import(TestSecurityConfig.class)
class NotificationControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private NotificationService notificationService;

  @MockitoBean private NotificationPreferencesService notificationPreferencesService;

  @Test
  @DisplayName("[getNotifications] API 문서화 테스트")
  @WithAuthenticatedUser
  void getNotifications() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    GetNotificationsResponse response =
        GetNotificationsResponseSnippets.getGetNotificationsResponse();

    when(notificationService.getNotifications(eq(user.userId()), eq(10), eq(null)))
        .thenReturn(response);

    mockMvc
        .perform(get("/notifications"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetNotifications",
                ResourceSnippetParameters.builder()
                    .tag("notification")
                    .summary("Get Notifications")
                    .description("Get Notifications")
                    .responseSchema(schema(GetNotificationsResponse.class.getSimpleName())),
                null,
                preprocessResponse(prettyPrint()),
                Function.identity(),
                GetNotificationsResponseSnippets.getNotificationsResponseFields()));
  }

  @Test
  @DisplayName("[markAsRead] API 문서화 테스트")
  @WithAuthenticatedUser
  void markAsRead() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    doNothing().when(notificationService).markAsRead(eq(user.userId()), eq(1L));

    mockMvc
        .perform(patch("/notifications/{notificationId}/read", 1L))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "MarkNotificationAsRead",
                ResourceSnippetParameters.builder()
                    .tag("notification")
                    .summary("Mark Notification As Read")
                    .description("Mark Notification As Read"),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("notificationId").description("Notification ID"))));
  }

  @Test
  @DisplayName("[markAllAsRead] API 문서화 테스트")
  @WithAuthenticatedUser
  void markAllAsRead() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    doNothing().when(notificationService).markAllAsRead(eq(user.userId()));

    mockMvc
        .perform(patch("/notifications/read-all"))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "MarkAllNotificationsAsRead",
                ResourceSnippetParameters.builder()
                    .tag("notification")
                    .summary("Mark All Notifications As Read")
                    .description("Mark All Notifications As Read")));
  }

  @Test
  @DisplayName("[getPreferences] API 문서화 테스트")
  @WithAuthenticatedUser
  void getPreferences() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    GetNotificationPreferencesResponse response =
        GetNotificationPreferencesResponseSnippets.getGetNotificationPreferencesResponse();

    when(notificationPreferencesService.getPreferences(eq(user.userId()))).thenReturn(response);

    mockMvc
        .perform(get("/notifications/preferences"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetNotificationPreferences",
                ResourceSnippetParameters.builder()
                    .tag("notification")
                    .summary("Get Notification Preferences")
                    .description("Get Notification Preferences")
                    .responseSchema(
                        schema(GetNotificationPreferencesResponse.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                GetNotificationPreferencesResponseSnippets
                    .getNotificationPreferencesResponseFields()));
  }

  @Test
  @DisplayName("[updatePreferences] API 문서화 테스트")
  @WithAuthenticatedUser
  void updatePreferences() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    UpdateNotificationPreferencesRequest request =
        UpdateNotificationPreferencesRequestSnippets.getUpdateNotificationPreferencesRequest();

    doNothing()
        .when(notificationPreferencesService)
        .updatePreferences(eq(user.userId()), eq(request));

    mockMvc
        .perform(
            patch("/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "UpdateNotificationPreferences",
                ResourceSnippetParameters.builder()
                    .tag("notification")
                    .summary("Update Notification Preferences")
                    .description("Update Notification Preferences")
                    .requestSchema(
                        schema(UpdateNotificationPreferencesRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                UpdateNotificationPreferencesRequestSnippets
                    .getUpdateNotificationPreferencesRequestFields()));
  }
}
