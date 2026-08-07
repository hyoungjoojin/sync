package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.promotion.dto.response.ActivePromotionResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class ActivePromotionResponseSnippets {

  public static List<ActivePromotionResponse> getActivePromotionResponses() {
    return List.of(
        new ActivePromotionResponse(
            1L,
            List.of(PromotionFieldSnippets.getPromotionFieldResponse()),
            "official-announcements",
            "beta-reward-program",
            PromotionSignupResponseSnippets.getPromotionSignupResponse()),
        new ActivePromotionResponse(2L, List.of(), null, null, null));
  }

  public static ResponseFieldsSnippet getActivePromotionResponseListFields() {
    List<FieldDescriptor> fields =
        new ArrayList<>(
            List.of(
                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("프로모션 ID"),
                fieldWithPath("[].fields").type(JsonFieldType.ARRAY).description("신청 시 수집하는 필드 목록"),
                fieldWithPath("[].projectHandle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글이 속한 프로젝트 핸들")
                    .optional(),
                fieldWithPath("[].postSlug")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 슬러그")
                    .optional(),
                fieldWithPath("[].signup")
                    .type(JsonFieldType.OBJECT)
                    .description("내 신청 정보")
                    .optional()));
    fields.addAll(PromotionFieldSnippets.getPromotionFieldResponseFields("[].fields[]."));
    fields.add(
        fieldWithPath("[].signup.attachment")
            .type(JsonFieldType.OBJECT)
            .description("제출한 데이터")
            .optional());
    fields.add(
        fieldWithPath("[].signup.attachment.phoneNumber")
            .type(JsonFieldType.STRING)
            .description("등록한 전화번호")
            .optional());
    fields.add(
        fieldWithPath("[].signup.createdAt")
            .type(JsonFieldType.STRING)
            .description("신청 시각")
            .optional());

    return responseFields(fields);
  }
}
