package com.skkil.sync.post.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.post.dto.request.CreateTagRequest;
import com.skkil.sync.post.dto.response.CreateTagResponse;
import com.skkil.sync.post.dto.response.GetTagsResponse;
import com.skkil.sync.post.service.TagService;
import com.skkil.sync.post.snippets.CreateTagRequestSnippets;
import com.skkil.sync.post.snippets.CreateTagResponseSnippets;
import com.skkil.sync.post.snippets.GetTagsResponseSnippets;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AdminTagController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class AdminTagControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private TagService tagService;

  @Test
  @DisplayName("[getAllTags] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void getAllTags() throws Exception {
    GetTagsResponse response = GetTagsResponseSnippets.getGetTagsResponse();

    when(tagService.getAllTagsForAdmin()).thenReturn(response);

    mockMvc
        .perform(get("/admin/tags"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetAdminTags",
                ResourceSnippetParameters.builder()
                    .tag("tag")
                    .summary("Get Admin Tags")
                    .description("모든 전역 태그를 조회합니다. (관리자 전용)")
                    .responseSchema(schema(GetTagsResponse.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                GetTagsResponseSnippets.getGetTagsResponseFields()));
  }

  @Test
  @DisplayName("[createTag] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void createTag() throws Exception {
    CreateTagRequest request = CreateTagRequestSnippets.getCreateTagRequest();
    CreateTagResponse response = CreateTagResponseSnippets.getCreateTagResponse();

    when(tagService.createTag(eq(request))).thenReturn(response);

    mockMvc
        .perform(
            post("/admin/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "AdminCreateTag",
                ResourceSnippetParameters.builder()
                    .tag("tag")
                    .summary("Admin Create Tag")
                    .description("검증된 전역 태그를 생성합니다. (관리자 전용)")
                    .requestSchema(schema(CreateTagRequest.class.getSimpleName()))
                    .responseSchema(schema(CreateTagResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                CreateTagRequestSnippets.getCreateTagRequestFields(),
                CreateTagResponseSnippets.getCreateTagResponseFields()));
  }

  @Test
  @DisplayName("[verifyTag] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void verifyTag() throws Exception {
    String name = "java";

    doNothing().when(tagService).verifyTag(name);

    mockMvc
        .perform(patch("/admin/tags/{name}/verify", name).with(csrf()))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AdminVerifyTag",
                ResourceSnippetParameters.builder()
                    .tag("tag")
                    .summary("Admin Verify Tag")
                    .description("전역 태그를 검증 상태로 변경합니다. (관리자 전용)"),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                null,
                Function.identity(),
                pathParameters(parameterWithName("name").description("태그 이름"))));
  }

  @Test
  @DisplayName("[unverifyTag] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void unverifyTag() throws Exception {
    String name = "java";

    doNothing().when(tagService).unverifyTag(name);

    mockMvc
        .perform(patch("/admin/tags/{name}/unverify", name).with(csrf()))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AdminUnverifyTag",
                ResourceSnippetParameters.builder()
                    .tag("tag")
                    .summary("Admin Unverify Tag")
                    .description("전역 태그의 검증 상태를 해제합니다. (관리자 전용)"),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                null,
                Function.identity(),
                pathParameters(parameterWithName("name").description("태그 이름"))));
  }

  @Test
  @DisplayName("[deleteTag] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void deleteTag() throws Exception {
    String name = "java";

    doNothing().when(tagService).rejectTag(name);

    mockMvc
        .perform(delete("/admin/tags/{name}", name).with(csrf()))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AdminDeleteTag",
                ResourceSnippetParameters.builder()
                    .tag("tag")
                    .summary("Admin Delete Tag")
                    .description("전역 태그를 삭제합니다. (관리자 전용)"),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                null,
                Function.identity(),
                pathParameters(parameterWithName("name").description("태그 이름"))));
  }
}
