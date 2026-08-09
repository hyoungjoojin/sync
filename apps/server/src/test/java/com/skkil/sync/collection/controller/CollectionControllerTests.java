package com.skkil.sync.collection.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.collection.dto.request.AddPostToCollectionRequest;
import com.skkil.sync.collection.dto.request.CreateCollectionRequest;
import com.skkil.sync.collection.dto.request.UpdateCollectionRequest;
import com.skkil.sync.collection.dto.response.CreateCollectionResponse;
import com.skkil.sync.collection.dto.response.GetCollectionPostsResponse;
import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.service.CollectionService;
import com.skkil.sync.collection.snippets.AddPostToCollectionRequestSnippets;
import com.skkil.sync.collection.snippets.CollectionSummarySnippets;
import com.skkil.sync.collection.snippets.CreateCollectionRequestSnippets;
import com.skkil.sync.collection.snippets.CreateCollectionResponseSnippets;
import com.skkil.sync.collection.snippets.GetCollectionPostsResponseSnippets;
import com.skkil.sync.collection.snippets.GetCollectionsResponseSnippets;
import com.skkil.sync.collection.snippets.UpdateCollectionRequestSnippets;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.security.WithAuthenticatedUserSecurityContextFactory;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.snippets.CursorPaginationRequestSnippets;
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

@WebMvcTest(CollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import(TestSecurityConfig.class)
class CollectionControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private CollectionService collectionService;

  @Test
  @DisplayName("[createCollection] API 문서화 테스트")
  @WithAuthenticatedUser
  void createCollection() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    CreateCollectionRequest request = CreateCollectionRequestSnippets.getCreateCollectionRequest();
    CreateCollectionResponse response =
        CreateCollectionResponseSnippets.getCreateCollectionResponse();

    when(collectionService.createPersonalCollection(eq(user.userId()), eq(request)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "CreateCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Create Collection")
                    .description("개인 컬렉션을 생성한다.")
                    .responseSchema(schema(CreateCollectionResponse.class.getSimpleName()))
                    .requestSchema(schema(CreateCollectionRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                CreateCollectionRequestSnippets.getCreateCollectionRequestFields(),
                CreateCollectionResponseSnippets.getCreateCollectionResponseFields()));
  }

  @Test
  @DisplayName("[createProjectCollection] API 문서화 테스트")
  @WithAuthenticatedUser
  void createProjectCollection() throws Exception {
    String handle = "my-project";
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    CreateCollectionRequest request = CreateCollectionRequestSnippets.getCreateCollectionRequest();
    CreateCollectionResponse response =
        CreateCollectionResponseSnippets.getCreateCollectionResponse();

    when(collectionService.createProjectCollection(eq(user.userId()), eq(handle), eq(request)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/projects/{handle}/collections", handle)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "CreateProjectCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Create Project Collection")
                    .description("프로젝트 소속 컬렉션을 생성한다. (프로젝트 팀원 권한 필요)")
                    .responseSchema(schema(CreateCollectionResponse.class.getSimpleName()))
                    .requestSchema(schema(CreateCollectionRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                null,
                Function.identity(),
                pathParameters(parameterWithName("handle").description("Project Handle")),
                CreateCollectionRequestSnippets.getCreateCollectionRequestFields(),
                CreateCollectionResponseSnippets.getCreateCollectionResponseFields()));
  }

  @Test
  @DisplayName("[getUserCollections] API 문서화 테스트")
  void getUserCollections() throws Exception {
    Long userId = 1L;
    GetCollectionsResponse response = GetCollectionsResponseSnippets.getGetCollectionsResponse();

    when(collectionService.getUserCollections(any(), eq(userId), any())).thenReturn(response);

    mockMvc
        .perform(get("/users/{userId}/collections", userId).queryParam("postHandle", "my-post"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetUserCollections",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Get User Collections")
                    .description("한 사용자의 개인 컬렉션 목록을 조회한다. 비공개 컬렉션은 본인에게만 보인다.")
                    .responseSchema(schema(GetCollectionsResponse.class.getSimpleName())),
                null,
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("userId").description("User ID")),
                queryParameters(
                    parameterWithName("postHandle")
                        .description("주어지면 이 게시글(slug)을 담고 있는 컬렉션의 멤버십을 memberships 로 표시한다.")
                        .optional()),
                GetCollectionsResponseSnippets.getGetCollectionsResponseFields()));
  }

  @Test
  @DisplayName("[getProjectCollections] API 문서화 테스트")
  void getProjectCollections() throws Exception {
    String handle = "my-project";
    GetCollectionsResponse response = GetCollectionsResponseSnippets.getGetCollectionsResponse();

    when(collectionService.getProjectCollections(any(), eq(handle), any())).thenReturn(response);

    mockMvc
        .perform(get("/projects/{handle}/collections", handle).queryParam("postHandle", "my-post"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetProjectCollections",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Get Project Collections")
                    .description(
                        "한 프로젝트의 컬렉션 목록을 조회한다. 팀원은 모든 컬렉션을, 그 외에는 공개 프로젝트의 공개 컬렉션만 볼 수 있다.")
                    .responseSchema(schema(GetCollectionsResponse.class.getSimpleName())),
                null,
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("handle").description("Project Handle")),
                queryParameters(
                    parameterWithName("postHandle")
                        .description("주어지면 이 게시글(slug)을 담고 있는 컬렉션의 멤버십을 memberships 로 표시한다.")
                        .optional()),
                GetCollectionsResponseSnippets.getGetCollectionsResponseFields()));
  }

  @Test
  @DisplayName("[getCollection] API 문서화 테스트")
  void getCollection() throws Exception {
    String externalId = "my-collection-a1b2c3d4";
    CollectionSummary response = CollectionSummarySnippets.getCollectionSummary();

    when(collectionService.getCollection(eq(externalId))).thenReturn(response);

    mockMvc
        .perform(get("/collections/{externalId}", externalId))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Get Collection")
                    .description("컬렉션 메타데이터를 조회한다. (게시글 항목은 별도 API 로 페이지네이션한다)")
                    .responseSchema(schema(CollectionSummary.class.getSimpleName())),
                null,
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID")),
                responseFields(CollectionSummarySnippets.getCollectionSummaryFields(""))));
  }

  @Test
  @DisplayName("[getCollectionPosts] API 문서화 테스트")
  void getCollectionPosts() throws Exception {
    String externalId = "my-collection-a1b2c3d4";
    CursorPaginationRequest pagination =
        CursorPaginationRequestSnippets.getCursorPaginationRequest();
    GetCollectionPostsResponse response =
        GetCollectionPostsResponseSnippets.getGetCollectionPostsResponse();

    when(collectionService.getCollectionPosts(eq(externalId), any(), eq(pagination)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/collections/{externalId}/posts", externalId)
                .params(CursorPaginationRequestSnippets.getCursorPaginationRequestQueryParams()))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetCollectionPosts",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Get Collection Posts")
                    .description(
                        "컬렉션 안의 게시글 항목을 커서로 페이지네이션한다. 열람 불가·삭제된 항목은 정보를 노출하지 않는 tombstone 으로 내려온다.")
                    .responseSchema(schema(GetCollectionPostsResponse.class.getSimpleName())),
                null,
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID")),
                CursorPaginationRequestSnippets.getCursorPaginationRequestParameters(),
                GetCollectionPostsResponseSnippets.getCollectionPostsResponseFields()));
  }

  @Test
  @DisplayName("[updateCollection] API 문서화 테스트")
  @WithAuthenticatedUser
  void updateCollection() throws Exception {
    String externalId = "my-collection-a1b2c3d4";
    UpdateCollectionRequest request = UpdateCollectionRequestSnippets.getUpdateCollectionRequest();

    mockMvc
        .perform(
            patch("/collections/{externalId}", externalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "UpdateCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Update Collection")
                    .description("컬렉션 메타데이터를 수정한다. 생략된 필드는 기존 값을 유지한다. (컬렉션 편집 권한 필요)")
                    .requestSchema(schema(UpdateCollectionRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID")),
                UpdateCollectionRequestSnippets.getUpdateCollectionRequestFields()));
  }

  @Test
  @DisplayName("[deleteCollection] API 문서화 테스트")
  @WithAuthenticatedUser
  void deleteCollection() throws Exception {
    String externalId = "my-collection-a1b2c3d4";

    mockMvc
        .perform(delete("/collections/{externalId}", externalId))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "DeleteCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Delete Collection")
                    .description("컬렉션을 삭제한다. (컬렉션 삭제 권한 필요)"),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID"))));
  }

  @Test
  @DisplayName("[addPost] API 문서화 테스트")
  @WithAuthenticatedUser
  void addPost() throws Exception {
    String externalId = "my-collection-a1b2c3d4";
    AddPostToCollectionRequest request =
        AddPostToCollectionRequestSnippets.getAddPostToCollectionRequest();

    mockMvc
        .perform(
            post("/collections/{externalId}/posts", externalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "AddPostToCollection",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Add Post To Collection")
                    .description(
                        "컬렉션에 게시글을 추가한다. 게시글은 (projectHandle, postHandle) 튜플로 참조하며, 요청자가 현재 열람 가능한 게시글만 추가할 수 있다. (컬렉션 편집 권한 필요)")
                    .requestSchema(schema(AddPostToCollectionRequest.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID")),
                AddPostToCollectionRequestSnippets.getAddPostToCollectionRequestFields()));
  }

  @Test
  @DisplayName("[removeItem] API 문서화 테스트")
  @WithAuthenticatedUser
  void removeItem() throws Exception {
    String externalId = "my-collection-a1b2c3d4";

    mockMvc
        .perform(delete("/collections/{externalId}/items/{collectionPostId}", externalId, 1L))
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "RemoveCollectionItem",
                ResourceSnippetParameters.builder()
                    .tag("collection")
                    .summary("Remove Collection Item")
                    .description(
                        "컬렉션 항목을 항목 ID(collectionPostId) 로 제거한다. 원본이 삭제·숨김된 tombstone 항목도 제거할 수 있다. (컬렉션 편집 권한 필요)"),
                null,
                null,
                Function.identity(),
                pathParameters(
                    parameterWithName("externalId").description("Collection External ID"),
                    parameterWithName("collectionPostId").description("Collection Item ID"))));
  }
}
