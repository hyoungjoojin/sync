package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.promotion.dto.request.UpdatePromotionRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class UpdatePromotionRequestSnippets {

  public static UpdatePromotionRequest getUpdatePromotionRequest() {
    return new UpdatePromotionRequest(
        List.of(PromotionFieldSnippets.getPromotionFieldRequest()),
        "official-announcements",
        "beta-reward-program");
  }

  public static RequestFieldsSnippet getUpdatePromotionRequestFields() {
    List<FieldDescriptor> fields =
        new ArrayList<>(
            List.of(
                fieldWithPath("fields")
                    .type(JsonFieldType.ARRAY)
                    .description("신청 시 수집할 필드 목록. 전체 교체됩니다."),
                fieldWithPath("projectHandle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글이 속한 프로젝트 핸들"),
                fieldWithPath("postSlug")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 슬러그. 이미 생성된 게시글이어야 합니다.")));
    fields.addAll(PromotionFieldSnippets.getPromotionFieldRequestFields("fields[]."));

    return requestFields(fields);
  }
}
