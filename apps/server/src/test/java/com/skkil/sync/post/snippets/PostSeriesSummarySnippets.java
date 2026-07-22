package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.post.dto.summary.PostSeriesSummary;
import com.skkil.sync.post.model.PostScope;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class PostSeriesSummarySnippets {

  public static PostSeriesSummary getPostSeriesSummary() {
    return new PostSeriesSummary(
        "series-external-id", "시리즈 이름", PostScope.WORKSPACE, 3L, 1L, "project-handle");
  }

  public static List<FieldDescriptor> getPostSeriesSummaryFields(String prefix) {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(
        fieldWithPath(prefix + "externalId").type(JsonFieldType.STRING).description("시리즈 외부 식별자"));
    fields.add(fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("시리즈 이름"));
    fields.add(
        fieldWithPath(prefix + "scope")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("시리즈 공개 범위 (PUBLIC: 개인 시리즈, WORKSPACE: 프로젝트 시리즈)")
            .attributes(RestDocsUtils.getEnumAttributes(PostScope.class)));
    fields.add(
        fieldWithPath(prefix + "postCount")
            .type(JsonFieldType.NUMBER)
            .description("시리즈에 포함된 게시글 수"));
    fields.add(
        fieldWithPath(prefix + "creatorId").type(JsonFieldType.NUMBER).description("시리즈 생성자 ID"));
    fields.add(
        fieldWithPath(prefix + "projectHandle")
            .type(JsonFieldType.STRING)
            .description("프로젝트 시리즈인 경우 소속 프로젝트 핸들 (개인 시리즈인 경우 없음)")
            .optional());
    return fields;
  }

  public static ResponseFieldsSnippet getPostSeriesSummaryResponseFields() {
    return responseFields(getPostSeriesSummaryFields(""));
  }
}
