package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.model.CollectionScope;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetCollectionsResponseSnippets {

  public static GetCollectionsResponse getGetCollectionsResponse() {
    CollectionSummary personal = CollectionSummarySnippets.getCollectionSummary();
    CollectionSummary workspace =
        new CollectionSummary(
            "team-picks-e5f6g7h8",
            "Team Picks",
            null,
            CollectionScope.WORKSPACE,
            true,
            5L,
            1L,
            "my-project");

    return new GetCollectionsResponse(
        List.of(personal, workspace),
        List.of(new GetCollectionsResponse.Membership("team-picks-e5f6g7h8", 1L)));
  }

  public static ResponseFieldsSnippet getGetCollectionsResponseFields() {
    List<FieldDescriptor> fields = new java.util.ArrayList<>();
    fields.add(fieldWithPath("collections").type(JsonFieldType.ARRAY).description("컬렉션 목록"));
    fields.addAll(CollectionSummarySnippets.getCollectionSummaryFields("collections[]."));
    fields.add(
        fieldWithPath("memberships")
            .type(JsonFieldType.ARRAY)
            .description("postHandle 쿼리 파라미터가 주어졌을 때, 해당 게시글을 담고 있는 컬렉션의 멤버십 목록 (없으면 생략)")
            .optional());
    fields.add(
        fieldWithPath("memberships[].collectionExternalId")
            .type(JsonFieldType.STRING)
            .description("게시글을 담고 있는 컬렉션의 외부 식별자")
            .optional());
    fields.add(
        fieldWithPath("memberships[].collectionPostId")
            .type(JsonFieldType.NUMBER)
            .description("해당 컬렉션 안에서 이 게시글이 위치한 항목 ID (제거 시 사용)")
            .optional());
    return responseFields(fields);
  }
}
