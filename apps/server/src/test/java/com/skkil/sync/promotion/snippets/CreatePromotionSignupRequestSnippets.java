package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.promotion.dto.request.CreatePromotionSignupRequest;
import java.util.Map;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class CreatePromotionSignupRequestSnippets {

  public static CreatePromotionSignupRequest getCreatePromotionSignupRequest() {
    return new CreatePromotionSignupRequest(Map.of("phoneNumber", "01012345678"));
  }

  public static RequestFieldsSnippet getCreatePromotionSignupRequestFields() {
    return requestFields(
        fieldWithPath("attachment")
            .type(JsonFieldType.OBJECT)
            .description("제출할 데이터. 키는 해당 프로모션에 등록된 필드의 key와 일치해야 합니다."),
        fieldWithPath("attachment.phoneNumber")
            .type(JsonFieldType.STRING)
            .description("전화번호 (숫자만)")
            .optional());
  }
}
