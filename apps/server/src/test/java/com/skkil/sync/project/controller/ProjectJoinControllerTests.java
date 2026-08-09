package com.skkil.sync.project.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.project.dto.response.GetMyProjectJoinRequestsResponse;
import com.skkil.sync.project.dto.response.GetProjectJoinRequestsResponse;
import com.skkil.sync.project.service.ProjectJoinService;
import com.skkil.sync.project.snippets.GetMyProjectJoinRequestsResponseSnippets;
import com.skkil.sync.project.snippets.GetProjectJoinRequestsResponseSnippets;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectJoinController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class ProjectJoinControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ProjectJoinService projectJoinService;

  @Test
  @DisplayName("[joinProject] API 문서화 테스트")
  @WithAuthenticatedUser
  void joinProject() throws Exception {
    String projectHandle = "my-project";

    doNothing().when(projectJoinService).joinProject(anyLong(), eq(projectHandle));

    mockMvc
        .perform(post("/projects/{handle}/join", projectHandle))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "JoinProject",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Join Project")
                    .description("프로젝트에 참여합니다. OPEN 정책이면 즉시 팀원이 되고, REQUEST 정책이면 가입 요청이 생성됩니다."),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("handle").description("프로젝트 핸들"))));
  }

  @Test
  @DisplayName("[getJoinRequests] API 문서화 테스트")
  @WithAuthenticatedUser
  void getJoinRequests() throws Exception {
    String projectHandle = "my-project";
    GetProjectJoinRequestsResponse response =
        GetProjectJoinRequestsResponseSnippets.getGetProjectJoinRequestsResponse();

    when(projectJoinService.getJoinRequests(projectHandle)).thenReturn(response);

    mockMvc
        .perform(get("/projects/{handle}/join-requests", projectHandle))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetProjectJoinRequests",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Get Project Join Requests")
                    .description("프로젝트의 대기 중인 가입 요청 목록을 조회합니다.")
                    .responseSchema(schema(GetProjectJoinRequestsResponse.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("handle").description("프로젝트 핸들")),
                GetProjectJoinRequestsResponseSnippets.getGetProjectJoinRequestsResponseFields()));
  }

  @Test
  @DisplayName("[approveJoinRequest] API 문서화 테스트")
  @WithAuthenticatedUser
  void approveJoinRequest() throws Exception {
    String projectHandle = "my-project";
    Long requestId = 1L;

    doNothing()
        .when(projectJoinService)
        .approveJoinRequest(anyLong(), eq(projectHandle), eq(requestId));

    mockMvc
        .perform(
            post("/projects/{handle}/join-requests/{requestId}/approve", projectHandle, requestId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "ApproveProjectJoinRequest",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Approve Project Join Request")
                    .description("프로젝트 가입 요청을 승인합니다. 승인 시 요청자가 팀원으로 추가됩니다."),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("handle").description("프로젝트 핸들"),
                    parameterWithName("requestId").description("가입 요청 ID"))));
  }

  @Test
  @DisplayName("[declineJoinRequest] API 문서화 테스트")
  @WithAuthenticatedUser
  void declineJoinRequest() throws Exception {
    String projectHandle = "my-project";
    Long requestId = 1L;

    doNothing().when(projectJoinService).declineJoinRequest(projectHandle, requestId);

    mockMvc
        .perform(
            post("/projects/{handle}/join-requests/{requestId}/decline", projectHandle, requestId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "DeclineProjectJoinRequest",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Decline Project Join Request")
                    .description("프로젝트 가입 요청을 거절합니다."),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("handle").description("프로젝트 핸들"),
                    parameterWithName("requestId").description("가입 요청 ID"))));
  }

  @Test
  @DisplayName("[getMyJoinRequests] API 문서화 테스트")
  @WithAuthenticatedUser
  void getMyJoinRequests() throws Exception {
    GetMyProjectJoinRequestsResponse response =
        GetMyProjectJoinRequestsResponseSnippets.getGetMyProjectJoinRequestsResponse();

    when(projectJoinService.getMyJoinRequests(anyLong())).thenReturn(response);

    mockMvc
        .perform(get("/join-requests"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetMyProjectJoinRequests",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Get My Project Join Requests")
                    .description("내가 보낸 대기 중인 프로젝트 가입 요청 목록을 조회합니다.")
                    .responseSchema(schema(GetMyProjectJoinRequestsResponse.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                GetMyProjectJoinRequestsResponseSnippets
                    .getGetMyProjectJoinRequestsResponseFields()));
  }

  @Test
  @DisplayName("[cancelJoinRequest] API 문서화 테스트")
  @WithAuthenticatedUser
  void cancelJoinRequest() throws Exception {
    Long requestId = 1L;

    doNothing().when(projectJoinService).cancelJoinRequest(anyLong(), eq(requestId));

    mockMvc
        .perform(delete("/join-requests/{requestId}", requestId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "CancelProjectJoinRequest",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Cancel Project Join Request")
                    .description("내가 보낸 프로젝트 가입 요청을 취소합니다."),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("requestId").description("가입 요청 ID"))));
  }
}
