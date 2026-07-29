package com.skkil.sync.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.config.TestSecurityConfig;
import com.skkil.sync.common.security.WithAuthenticatedUser;
import com.skkil.sync.common.security.WithAuthenticatedUserSecurityContextFactory;
import com.skkil.sync.config.SecurityConfig;
import com.skkil.sync.user.dto.response.GetChannelTalkIdentityResponse;
import com.skkil.sync.user.service.ChannelTalkIdentityService;
import com.skkil.sync.user.snippets.GetChannelTalkIdentityResponseSnippets;
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

@WebMvcTest(ChannelTalkIdentityController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class ChannelTalkIdentityControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ChannelTalkIdentityService channelTalkIdentityService;

  @Test
  @DisplayName("[getChannelTalkIdentity] API 문서화 테스트")
  @WithAuthenticatedUser
  void getChannelTalkIdentity() throws Exception {
    AuthenticatedUser user = WithAuthenticatedUserSecurityContextFactory.getAuthenticatedUser();
    GetChannelTalkIdentityResponse response =
        GetChannelTalkIdentityResponseSnippets.getGetChannelTalkIdentityResponse();

    when(channelTalkIdentityService.getChannelTalkIdentity(eq(user.userId()))).thenReturn(response);

    mockMvc
        .perform(get("/channel-talk/identity"))
        .andExpect(status().isOk())
        .andDo(
            document(
                "GetChannelTalkIdentity",
                ResourceSnippetParameters.builder()
                    .tag("channel-talk")
                    .summary("Get Channel Talk Identity")
                    .description("Get Channel Talk Identity")
                    .responseSchema(schema(GetChannelTalkIdentityResponse.class.getSimpleName())),
                null,
                null,
                Function.identity(),
                GetChannelTalkIdentityResponseSnippets.getChannelTalkIdentityResponseFields()));
  }
}
