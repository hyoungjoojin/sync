package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.promotion.dto.request.PromotionFieldRequest;
import com.skkil.sync.promotion.dto.response.PromotionFieldResponse;
import com.skkil.sync.promotion.model.PromotionFieldType;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class PromotionFieldSnippets {

  public static PromotionFieldRequest getPromotionFieldRequest() {
    return new PromotionFieldRequest("phoneNumber", PromotionFieldType.PHONE_NUMBER, "전화번호", true);
  }

  public static PromotionFieldResponse getPromotionFieldResponse() {
    return new PromotionFieldResponse("phoneNumber", PromotionFieldType.PHONE_NUMBER, "전화번호", true);
  }

  public static List<FieldDescriptor> getPromotionFieldRequestFields(String prefix) {
    return List.of(
        fieldWithPath(prefix + "key").type(JsonFieldType.STRING).description("필드 키"),
        fieldWithPath(prefix + "type")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("필드 입력 타입")
            .attributes(RestDocsUtils.getEnumAttributes(PromotionFieldType.class)),
        fieldWithPath(prefix + "label").type(JsonFieldType.STRING).description("필드 라벨"),
        fieldWithPath(prefix + "required").type(JsonFieldType.BOOLEAN).description("필수 여부"));
  }

  public static List<FieldDescriptor> getPromotionFieldResponseFields(String prefix) {
    return getPromotionFieldRequestFields(prefix);
  }
}
