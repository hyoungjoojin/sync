package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.model.CollectionScope;
import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class CollectionSummarySnippets {

  public static CollectionSummary getCollectionSummary() {
    return new CollectionSummary(
        "my-collection-a1b2c3d4",
        "My Collection",
        "A collection of posts",
        CollectionScope.PERSONAL,
        true,
        3L,
        1L,
        null,
        null);
  }

  public static List<FieldDescriptor> getCollectionSummaryFields(String prefix) {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(
        fieldWithPath(prefix + "externalId").type(JsonFieldType.STRING).description("컬렉션 외부 식별자"));
    fields.add(fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("컬렉션 이름"));
    fields.add(
        fieldWithPath(prefix + "description")
            .type(JsonFieldType.STRING)
            .description("컬렉션 설명")
            .optional());
    fields.add(
        fieldWithPath(prefix + "scope")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("컬렉션 범위 (PERSONAL: 개인, WORKSPACE: 프로젝트 소속)")
            .attributes(RestDocsUtils.getEnumAttributes(CollectionScope.class)));
    fields.add(fieldWithPath(prefix + "isPublic").type(JsonFieldType.BOOLEAN).description("공개 여부"));
    fields.add(
        fieldWithPath(prefix + "postCount").type(JsonFieldType.NUMBER).description("컬렉션에 담긴 항목 수"));
    fields.add(
        fieldWithPath(prefix + "creatorId").type(JsonFieldType.NUMBER).description("컬렉션 생성자 ID"));
    fields.add(
        fieldWithPath(prefix + "projectHandle")
            .type(JsonFieldType.STRING)
            .description("소속 프로젝트 핸들 (WORKSPACE 범위인 경우, PERSONAL 이면 없음)")
            .optional());
    fields.add(
        fieldWithPath(prefix + "containsPost")
            .type(JsonFieldType.BOOLEAN)
            .description("postHandle 쿼리 파라미터가 주어졌을 때, 해당 게시글이 이 컬렉션에 담겨 있는지 여부 (없으면 생략)")
            .optional());
    return fields;
  }
}
