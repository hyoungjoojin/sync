package com.skkil.sync.promotion.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.promotion.dto.request.CreatePromotionRequest;
import com.skkil.sync.promotion.dto.request.UpdatePromotionRequest;
import com.skkil.sync.promotion.dto.response.AdminPromotionSignupResponse;
import com.skkil.sync.promotion.dto.response.PromotionResponse;
import com.skkil.sync.promotion.service.PromotionService;
import com.skkil.sync.promotion.service.PromotionSignupService;
import com.skkil.sync.promotion.snippets.AdminPromotionSignupResponseSnippets;
import com.skkil.sync.promotion.snippets.CreatePromotionRequestSnippets;
import com.skkil.sync.promotion.snippets.PromotionResponseSnippets;
import com.skkil.sync.promotion.snippets.UpdatePromotionRequestSnippets;
import com.skkil.sync.user.constant.Role;
import java.util.List;
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

@WebMvcTest(AdminPromotionController.class)
@AutoConfigureMockMvc(addFilters = true)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class AdminPromotionControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private PromotionService promotionService;

  @MockitoBean private PromotionSignupService promotionSignupService;

  @Test
  @DisplayName("[getPromotions] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void getPromotions() throws Exception {
    PromotionResponse response = PromotionResponseSnippets.getPromotionResponse();

    when(promotionService.getAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/admin/promotions"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetPromotions",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Get Promotions")
                    .description("모든 프로모션 목록을 조회합니다. 관리자만 접근할 수 있습니다.")
                    .responseSchema(schema(PromotionResponse.class.getSimpleName() + "List")),
                null,
                null,
                Function.identity(),
                PromotionResponseSnippets.getPromotionResponseListFields()));
  }

  @Test
  @DisplayName("[createPromotion] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void createPromotion() throws Exception {
    CreatePromotionRequest request = CreatePromotionRequestSnippets.getCreatePromotionRequest();
    PromotionResponse response = PromotionResponseSnippets.getPromotionResponse();

    when(promotionService.create(eq(request))).thenReturn(response);

    mockMvc
        .perform(
            post("/admin/promotions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(
            document(
                "CreatePromotion",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Create Promotion")
                    .description("새 프로모션을 생성합니다. 관리자만 접근할 수 있습니다. 생성 직후에는 비활성 상태입니다.")
                    .requestSchema(schema(CreatePromotionRequest.class.getSimpleName()))
                    .responseSchema(schema(PromotionResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                CreatePromotionRequestSnippets.getCreatePromotionRequestFields(),
                PromotionResponseSnippets.getPromotionResponseFields()));
  }

  @Test
  @DisplayName("[updatePromotion] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void updatePromotion() throws Exception {
    Long promotionId = 1L;
    UpdatePromotionRequest request = UpdatePromotionRequestSnippets.getUpdatePromotionRequest();
    PromotionResponse response = PromotionResponseSnippets.getPromotionResponse();

    when(promotionService.update(eq(promotionId), eq(request))).thenReturn(response);

    mockMvc
        .perform(
            patch("/admin/promotions/{id}", promotionId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(
            document(
                "UpdatePromotion",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Update Promotion")
                    .description("프로모션의 연결된 게시글과 신청 필드를 수정합니다. 관리자만 접근할 수 있습니다.")
                    .requestSchema(schema(UpdatePromotionRequest.class.getSimpleName()))
                    .responseSchema(schema(PromotionResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("id").description("프로모션 ID")),
                UpdatePromotionRequestSnippets.getUpdatePromotionRequestFields(),
                PromotionResponseSnippets.getPromotionResponseFields()));
  }

  @Test
  @DisplayName("[activatePromotion] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void activatePromotion() throws Exception {
    Long promotionId = 1L;
    PromotionResponse response = PromotionResponseSnippets.getPromotionResponse();

    when(promotionService.activate(promotionId)).thenReturn(response);

    mockMvc
        .perform(patch("/admin/promotions/{id}/activate", promotionId).with(csrf()))
        .andExpect(status().isOk())
        .andDo(
            document(
                "ActivatePromotion",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Activate Promotion")
                    .description("프로모션을 활성화합니다. 관리자만 접근할 수 있습니다.")
                    .responseSchema(schema(PromotionResponse.class.getSimpleName())),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("id").description("프로모션 ID")),
                PromotionResponseSnippets.getPromotionResponseFields()));
  }

  @Test
  @DisplayName("[deactivatePromotion] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void deactivatePromotion() throws Exception {
    Long promotionId = 1L;
    PromotionResponse response = PromotionResponseSnippets.getPromotionResponse();

    when(promotionService.deactivate(promotionId)).thenReturn(response);

    mockMvc
        .perform(patch("/admin/promotions/{id}/deactivate", promotionId).with(csrf()))
        .andExpect(status().isOk())
        .andDo(
            document(
                "DeactivatePromotion",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Deactivate Promotion")
                    .description("프로모션을 비활성화합니다. 관리자만 접근할 수 있습니다.")
                    .responseSchema(schema(PromotionResponse.class.getSimpleName())),
                preprocessRequest(RestDocsUtils.removeCsrfFormBody()),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("id").description("프로모션 ID")),
                PromotionResponseSnippets.getPromotionResponseFields()));
  }

  @Test
  @DisplayName("[getPromotionSignups] API 문서화 테스트")
  @WithAuthenticatedUser(role = Role.ADMIN)
  void getPromotionSignups() throws Exception {
    Long promotionId = 1L;
    List<AdminPromotionSignupResponse> response =
        AdminPromotionSignupResponseSnippets.getAdminPromotionSignupResponses();

    when(promotionSignupService.getSignups(promotionId)).thenReturn(response);

    mockMvc
        .perform(get("/admin/promotions/{id}/signups", promotionId))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetPromotionSignups",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Get Promotion Signups")
                    .description("프로모션에 신청한 유저 목록을 조회합니다. 관리자만 접근할 수 있습니다.")
                    .responseSchema(
                        schema(AdminPromotionSignupResponse.class.getSimpleName() + "List")),
                null,
                null,
                Function.identity(),
                pathParameters(parameterWithName("id").description("프로모션 ID")),
                AdminPromotionSignupResponseSnippets.getAdminPromotionSignupResponseListFields()));
  }

  @Test
  @DisplayName("[getPromotions] 로그인하지 않은 사용자는 접근할 수 없다")
  void getPromotions_unauthenticatedUser_shouldReturnUnauthorized() throws Exception {
    mockMvc.perform(get("/admin/promotions")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("[getPromotions] 관리자가 아닌 사용자는 접근할 수 없다")
  @WithAuthenticatedUser(role = Role.USER)
  void getPromotions_nonAdmin_shouldReturnForbidden() throws Exception {
    mockMvc.perform(get("/admin/promotions")).andExpect(status().isForbidden());
  }
}
