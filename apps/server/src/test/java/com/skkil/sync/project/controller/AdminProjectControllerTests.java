package com.skkil.sync.project.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.exception.ErrorCode;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.project.dto.summary.AdminProjectSummary;
import com.skkil.sync.project.exception.ProjectNotFoundException;
import com.skkil.sync.project.service.AdminProjectService;
import com.skkil.sync.project.snippets.AdminProjectSummarySnippets;
import com.skkil.sync.user.constant.Role;
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

@WebMvcTest(AdminProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class AdminProjectControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AdminProjectService adminProjectService;

  @Test
  @DisplayName("[searchProject] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void searchProject() throws Exception {
    String query = "project";
    AdminProjectSummary response = AdminProjectSummarySnippets.getAdminProjectSummary();

    when(adminProjectService.searchProject(eq(query))).thenReturn(response);

    mockMvc
        .perform(get("/admin/projects").queryParam("query", query))
        .andExpect(status().isOk())
        .andDo(
            document(
                "SearchAdminProject",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Search Admin Project")
                    .description(
                        "검색어와 가장 일치하는 프로젝트 한 개를 조회합니다. 비공개 프로젝트를 포함하며 관리자용 상세 정보를 제공합니다. (관리자 전용)")
                    .responseSchema(schema(AdminProjectSummary.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                queryParameters(parameterWithName("query").description("검색어 (프로젝트 이름, 핸들)")),
                AdminProjectSummarySnippets.getAdminProjectSummaryResponseFields()));
  }

  @Test
  @DisplayName("[searchProject] 일치하는 프로젝트가 없으면 404를 반환한다")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void searchProjectNotFound() throws Exception {
    String query = "not-exist";

    doThrow(new ProjectNotFoundException()).when(adminProjectService).searchProject(eq(query));

    mockMvc
        .perform(get("/admin/projects").queryParam("query", query))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.PROJECT_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[deleteProject] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void deleteProject() throws Exception {
    String handle = "my-project";

    doNothing().when(adminProjectService).deleteProject(eq(handle));

    mockMvc
        .perform(delete("/admin/projects/{handle}", handle).with(csrf()))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AdminDeleteProject",
                ResourceSnippetParameters.builder()
                    .tag("project")
                    .summary("Admin Delete Project")
                    .description("프로젝트를 삭제합니다. 소유자가 아니어도 삭제할 수 있습니다. (관리자 전용)"),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                null,
                Function.identity(),
                pathParameters(parameterWithName("handle").description("삭제할 프로젝트 핸들"))));
  }

  @Test
  @DisplayName("[deleteProject] 존재하지 않는 프로젝트면 404를 반환한다")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void deleteProjectNotFound() throws Exception {
    String handle = "not-exist";

    doThrow(new ProjectNotFoundException()).when(adminProjectService).deleteProject(eq(handle));

    mockMvc
        .perform(delete("/admin/projects/{handle}", handle).with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.PROJECT_NOT_FOUND.name()));
  }
}
