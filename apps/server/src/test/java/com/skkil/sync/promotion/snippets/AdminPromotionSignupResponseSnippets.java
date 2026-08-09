package com.skkil.sync.promotion.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.promotion.dto.response.AdminPromotionSignupResponse;
import com.skkil.sync.user.snippets.UserSummarySnippets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class AdminPromotionSignupResponseSnippets {

  public static List<AdminPromotionSignupResponse> getAdminPromotionSignupResponses() {
    return List.of(
        new AdminPromotionSignupResponse(
            1L,
            UserSummarySnippets.getUserSummary(),
            Map.of("phoneNumber", "01012345678"),
            Instant.EPOCH));
  }

  public static ResponseFieldsSnippet getAdminPromotionSignupResponseListFields() {
    FieldDescriptors fields =
        new FieldDescriptors(
            fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("신청 ID"),
            fieldWithPath("[].user").type(JsonFieldType.OBJECT).description("신청자 정보"),
            fieldWithPath("[].attachment").type(JsonFieldType.OBJECT).description("제출한 데이터"),
            fieldWithPath("[].attachment.phoneNumber")
                .type(JsonFieldType.STRING)
                .description("등록한 전화번호")
                .optional(),
            fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("신청 시각"));

    fields =
        fields.andWithPrefix(
            "[].user",
            UserSummarySnippets.getUserSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
