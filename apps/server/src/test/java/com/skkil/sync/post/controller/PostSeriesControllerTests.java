package com.skkil.sync.post.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.security.WithAuthenticatedUserSecurityContextFactory;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.post.dto.request.AddPostToPostSeriesRequest;
import com.skkil.sync.post.dto.request.CreatePostSeriesRequest;
import com.skkil.sync.post.dto.request.ReorderPostSeriesPostRequest;
import com.skkil.sync.post.dto.request.UpdatePostSeriesRequest;
import com.skkil.sync.post.dto.response.CreatePostSeriesResponse;
import com.skkil.sync.post.dto.response.GetPostSeriesListResponse;
import com.skkil.sync.post.dto.response.GetPostSeriesResponse;
import com.skkil.sync.post.service.PostSeriesService;
import com.skkil.sync.post.snippets.AddPostToPostSeriesRequestSnippets;
import com.skkil.sync.post.snippets.CreatePostSeriesRequestSnippets;
import com.skkil.sync.post.snippets.CreatePostSeriesResponseSnippets;
import com.skkil.sync.post.snippets.GetPostSeriesListResponseSnippets;
import com.skkil.sync.post.snippets.GetPostSeriesResponseSnippets;
import com.skkil.sync.post.snippets.ReorderPostSeriesPostRequestSnippets;
import com.skkil.sync.post.snippets.UpdatePostSeriesRequestSnippets;
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

@WebMvcTest(PostSeriesController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class PostSeriesControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private PostSeriesService seriesService;

  @Test
  @DisplayName("[createSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void createSeries() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    CreatePostSeriesRequest request = CreatePostSeriesRequestSnippets.getCreatePostSeriesRequest();
    CreatePostSeriesResponse response =
        CreatePostSeriesResponseSnippets.getCreatePostSeriesResponse();

    when(seriesService.createPersonalSeries(eq(user.userId()), eq(request))).thenReturn(response);

    mockMvc
        .perform(
            post("/series")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "CreatePostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Create Post Series")
                    .description("개인 시리즈를 생성합니다.")
                    .responseSchema(schema(CreatePostSeriesResponse.class.getSimpleName()))
                    .requestSchema(schema(CreatePostSeriesRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                CreatePostSeriesRequestSnippets.getCreatePostSeriesRequestFields(),
                CreatePostSeriesResponseSnippets.getCreatePostSeriesResponseFields()));
  }

  @Test
  @DisplayName("[createProjectSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void createProjectSeries() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    String handle = "project-handle";
    CreatePostSeriesRequest request = CreatePostSeriesRequestSnippets.getCreatePostSeriesRequest();
    CreatePostSeriesResponse response =
        CreatePostSeriesResponseSnippets.getCreatePostSeriesResponse();

    when(seriesService.createProjectSeries(eq(user.userId()), eq(handle), eq(request)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/projects/{handle}/series", handle)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "CreateProjectPostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Create Project Post Series")
                    .description("프로젝트에 시리즈를 생성합니다.")
                    .responseSchema(schema(CreatePostSeriesResponse.class.getSimpleName()))
                    .requestSchema(schema(CreatePostSeriesRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("handle").description("프로젝트 핸들")),
                CreatePostSeriesRequestSnippets.getCreatePostSeriesRequestFields(),
                CreatePostSeriesResponseSnippets.getCreatePostSeriesResponseFields()));
  }

  @Test
  @DisplayName("[getSeriesForPost] API 문서화 테스트")
  @WithAuthenticatedUser
  void getSeriesForPost() throws Exception {
    String slug = "test-slug";
    GetPostSeriesResponse response = GetPostSeriesResponseSnippets.getGetPostSeriesResponse();

    when(seriesService.getSeriesForPost(eq(slug), any())).thenReturn(response);

    mockMvc
        .perform(get("/posts/{slug}/series", slug))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetSeriesForPost",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Get Series For Post")
                    .description("게시글이 속한 시리즈와 그 시리즈의 게시글 목록을 조회합니다.")
                    .responseSchema(schema(GetPostSeriesResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("slug").description("게시글 슬러그")),
                GetPostSeriesResponseSnippets.getGetPostSeriesResponseFields()));
  }

  @Test
  @DisplayName("[getMyPersonalSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void getMyPersonalSeries() throws Exception {
    GetPostSeriesListResponse response =
        GetPostSeriesListResponseSnippets.getGetPostSeriesListResponse();

    when(seriesService.getMyPersonalSeries(any())).thenReturn(response);

    mockMvc
        .perform(get("/me/series"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetMyPostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Get My Post Series")
                    .description("로그인 사용자 본인의 개인 시리즈 목록을 조회합니다.")
                    .responseSchema(schema(GetPostSeriesListResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                GetPostSeriesListResponseSnippets.getGetPostSeriesListResponseFields()));
  }

  @Test
  @DisplayName("[getMyProjectSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void getMyProjectSeries() throws Exception {
    String handle = "project-handle";
    GetPostSeriesListResponse response =
        GetPostSeriesListResponseSnippets.getGetPostSeriesListResponse();

    when(seriesService.getMyProjectSeries(any(), eq(handle))).thenReturn(response);

    mockMvc
        .perform(get("/projects/{handle}/series", handle))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetProjectPostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Get Project Post Series")
                    .description("로그인 사용자가 해당 프로젝트에서 만든 본인 소유 시리즈 목록을 조회합니다.")
                    .responseSchema(schema(GetPostSeriesListResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("handle").description("프로젝트 핸들")),
                GetPostSeriesListResponseSnippets.getGetPostSeriesListResponseFields()));
  }

  @Test
  @DisplayName("[updateSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void updateSeries() throws Exception {
    String externalId = "series-external-id";
    UpdatePostSeriesRequest request = UpdatePostSeriesRequestSnippets.getUpdatePostSeriesRequest();

    doNothing().when(seriesService).updateSeries(eq(externalId), eq(request));

    mockMvc
        .perform(
            patch("/series/{externalId}", externalId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "UpdatePostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Update Post Series")
                    .description("시리즈 정보를 수정합니다.")
                    .requestSchema(schema(UpdatePostSeriesRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("externalId").description("시리즈 외부 식별자")),
                UpdatePostSeriesRequestSnippets.getUpdatePostSeriesRequestFields()));
  }

  @Test
  @DisplayName("[deleteSeries] API 문서화 테스트")
  @WithAuthenticatedUser
  void deleteSeries() throws Exception {
    String externalId = "series-external-id";

    doNothing().when(seriesService).deleteSeries(eq(externalId));

    mockMvc
        .perform(delete("/series/{externalId}", externalId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "DeletePostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Delete Post Series")
                    .description("시리즈를 삭제합니다."),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("externalId").description("시리즈 외부 식별자"))));
  }

  @Test
  @DisplayName("[addPost] API 문서화 테스트")
  @WithAuthenticatedUser
  void addPost() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    String externalId = "series-external-id";
    AddPostToPostSeriesRequest request =
        AddPostToPostSeriesRequestSnippets.getAddPostToPostSeriesRequest();

    doNothing().when(seriesService).addPost(eq(user.userId()), eq(externalId), eq(request));

    mockMvc
        .perform(
            post("/series/{externalId}/posts", externalId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AddPostToPostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Add Post To Series")
                    .description("시리즈에 게시글을 추가합니다.")
                    .requestSchema(schema(AddPostToPostSeriesRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("externalId").description("시리즈 외부 식별자")),
                AddPostToPostSeriesRequestSnippets.getAddPostToPostSeriesRequestFields()));
  }

  @Test
  @DisplayName("[reorderPost] API 문서화 테스트")
  @WithAuthenticatedUser
  void reorderPost() throws Exception {
    String externalId = "series-external-id";
    Long seriesPostId = 1L;
    ReorderPostSeriesPostRequest request =
        ReorderPostSeriesPostRequestSnippets.getReorderPostSeriesPostRequest();

    doNothing().when(seriesService).reorderPost(eq(externalId), eq(seriesPostId), eq(request));

    mockMvc
        .perform(
            patch("/series/{externalId}/posts/{seriesPostId}", externalId, seriesPostId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "ReorderPostSeriesPost",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Reorder Post In Series")
                    .description("시리즈 내 게시글의 순서를 변경합니다.")
                    .requestSchema(schema(ReorderPostSeriesPostRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("시리즈 외부 식별자"),
                    parameterWithName("seriesPostId").description("시리즈-게시글 연결 ID")),
                ReorderPostSeriesPostRequestSnippets.getReorderPostSeriesPostRequestFields()));
  }

  @Test
  @DisplayName("[removeItem] API 문서화 테스트")
  @WithAuthenticatedUser
  void removeItem() throws Exception {
    String externalId = "series-external-id";
    Long seriesPostId = 1L;

    doNothing().when(seriesService).removeItem(eq(externalId), eq(seriesPostId));

    mockMvc
        .perform(delete("/series/{externalId}/posts/{seriesPostId}", externalId, seriesPostId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "RemovePostFromPostSeries",
                ResourceSnippetParameters.builder()
                    .tag("post-series")
                    .summary("Remove Post From Series")
                    .description("시리즈에서 게시글을 제거합니다."),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("시리즈 외부 식별자"),
                    parameterWithName("seriesPostId").description("시리즈-게시글 연결 ID"))));
  }
}
