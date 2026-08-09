package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.promotion.dto.response.PromotionSignupResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class PromotionSignupResponseSnippets {

  public static PromotionSignupResponse getPromotionSignupResponse() {
    return new PromotionSignupResponse(Map.of("phoneNumber", "01012345678"), Instant.EPOCH);
  }

  public static ResponseFieldsSnippet getPromotionSignupResponseFields() {
    return responseFields(
        fieldWithPath("attachment").type(JsonFieldType.OBJECT).description("제출한 데이터"),
        fieldWithPath("attachment.phoneNumber")
            .type(JsonFieldType.STRING)
            .description("등록한 전화번호")
            .optional(),
        fieldWithPath("createdAt").type(JsonFieldType.STRING).description("신청 시각"));
  }
}
