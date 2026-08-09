package com.skkil.sync.user.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.user.dto.response.GetChannelTalkIdentityResponse;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetChannelTalkIdentityResponseSnippets {

  public static GetChannelTalkIdentityResponse getGetChannelTalkIdentityResponse() {
    return new GetChannelTalkIdentityResponse(
        "1", "99427c7bba36a6902c5fd6383f2fb0214d19b81023296b4bd6b9e024836afea2");
  }

  public static ResponseFieldsSnippet getChannelTalkIdentityResponseFields() {
    return responseFields(
        fieldWithPath("memberId")
            .type(JsonFieldType.STRING)
            .description("Channel Talk member identifier of the authenticated user"),
        fieldWithPath("memberHash")
            .type(JsonFieldType.STRING)
            .description(
                "HMAC-SHA256 of memberId. Null when the server has no secret key configured — "
                    + "the client must then boot Channel Talk anonymously.")
            .optional());
  }
}
