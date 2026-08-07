package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.promotion.dto.response.PromotionResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class PromotionResponseSnippets {

  public static PromotionResponse getPromotionResponse() {
    return new PromotionResponse(
        1L,
        true,
        List.of(PromotionFieldSnippets.getPromotionFieldResponse()),
        "official-announcements",
        "beta-reward-program",
        "베타 리워드 프로그램 안내",
        Instant.EPOCH);
  }

  public static ResponseFieldsSnippet getPromotionResponseFields() {
    List<FieldDescriptor> fields =
        new ArrayList<>(
            List.of(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("프로모션 ID"),
                fieldWithPath("active").type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                fieldWithPath("fields").type(JsonFieldType.ARRAY).description("신청 시 수집하는 필드 목록"),
                fieldWithPath("projectHandle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글이 속한 프로젝트 핸들")
                    .optional(),
                fieldWithPath("postSlug")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 슬러그")
                    .optional(),
                fieldWithPath("postTitle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 제목")
                    .optional(),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 시각")));
    fields.addAll(PromotionFieldSnippets.getPromotionFieldResponseFields("fields[]."));

    return responseFields(fields);
  }

  public static ResponseFieldsSnippet getPromotionResponseListFields() {
    List<FieldDescriptor> fields =
        new ArrayList<>(
            List.of(
                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("프로모션 ID"),
                fieldWithPath("[].active").type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                fieldWithPath("[].fields").type(JsonFieldType.ARRAY).description("신청 시 수집하는 필드 목록"),
                fieldWithPath("[].projectHandle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글이 속한 프로젝트 핸들")
                    .optional(),
                fieldWithPath("[].postSlug")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 슬러그")
                    .optional(),
                fieldWithPath("[].postTitle")
                    .type(JsonFieldType.STRING)
                    .description("연결된 게시글의 제목")
                    .optional(),
                fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성 시각")));
    fields.addAll(PromotionFieldSnippets.getPromotionFieldResponseFields("[].fields[]."));

    return responseFields(fields);
  }
}
