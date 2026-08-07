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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.security.WithAuthenticatedUserSecurityContextFactory;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.promotion.dto.request.CreatePromotionSignupRequest;
import com.skkil.sync.promotion.dto.response.ActivePromotionResponse;
import com.skkil.sync.promotion.dto.response.PromotionSignupResponse;
import com.skkil.sync.promotion.service.PromotionService;
import com.skkil.sync.promotion.service.PromotionSignupService;
import com.skkil.sync.promotion.snippets.ActivePromotionResponseSnippets;
import com.skkil.sync.promotion.snippets.CreatePromotionSignupRequestSnippets;
import com.skkil.sync.promotion.snippets.PromotionSignupResponseSnippets;
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

@WebMvcTest(PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class PromotionControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private PromotionService promotionService;

  @MockitoBean private PromotionSignupService promotionSignupService;

  @Test
  @DisplayName("[getActivePromotions] API 문서화 테스트")
  @WithAuthenticatedUser
  void getActivePromotions() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    var response = ActivePromotionResponseSnippets.getActivePromotionResponses();

    when(promotionService.getActiveForUser(user.userId())).thenReturn(response);

    mockMvc
        .perform(get("/promotions/active"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetActivePromotions",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Get Active Promotions")
                    .description("현재 진행 중인 모든 프로모션과 내 신청 상태를 조회합니다.")
                    .responseSchema(schema(ActivePromotionResponse.class.getSimpleName() + "List")),
                null,
                null,
                Function.identity(),
                ActivePromotionResponseSnippets.getActivePromotionResponseListFields()));
  }

  @Test
  @DisplayName("[submitSignup] API 문서화 테스트")
  @WithAuthenticatedUser
  void submitSignup() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    Long promotionId = 1L;
    CreatePromotionSignupRequest request =
        CreatePromotionSignupRequestSnippets.getCreatePromotionSignupRequest();
    PromotionSignupResponse response = PromotionSignupResponseSnippets.getPromotionSignupResponse();

    when(promotionSignupService.submit(eq(user.userId()), eq(promotionId), eq(request)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/promotions/{promotionId}/signups", promotionId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(
            document(
                "SubmitPromotionSignup",
                ResourceSnippetParameters.builder()
                    .tag("promotion")
                    .summary("Submit Promotion Signup")
                    .description("활성 프로모션에 신청합니다. 이미 신청한 경우 제출한 데이터를 갱신합니다.")
                    .requestSchema(schema(CreatePromotionSignupRequest.class.getSimpleName()))
                    .responseSchema(schema(PromotionSignupResponse.class.getSimpleName())),
                preprocessRequest(modifyHeaders().set("Content-Type", "application/json")),
                preprocessResponse(prettyPrint()),
                Function.identity(),
                pathParameters(parameterWithName("promotionId").description("프로모션 ID")),
                CreatePromotionSignupRequestSnippets.getCreatePromotionSignupRequestFields(),
                PromotionSignupResponseSnippets.getPromotionSignupResponseFields()));
  }
}
