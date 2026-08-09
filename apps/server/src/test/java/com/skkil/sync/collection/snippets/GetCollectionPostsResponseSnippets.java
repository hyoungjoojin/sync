package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.collection.dto.response.GetCollectionPostsResponse;
import com.skkil.sync.common.util.pagination.snippets.CursorPaginationResponseSnippets;
import com.skkil.sync.post.snippets.PostSummarySnippets;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetCollectionPostsResponseSnippets {

  public static GetCollectionPostsResponse getGetCollectionPostsResponse() {
    GetCollectionPostsResponse.Item item =
        new GetCollectionPostsResponse.Item(1L, PostSummarySnippets.getPostSummary());

    return new GetCollectionPostsResponse(CursorPaginationResponseSnippets.of(List.of(item)));
  }

  public static ResponseFieldsSnippet getCollectionPostsResponseFields() {
    FieldDescriptors fields =
        new FieldDescriptors()
            .and(fieldWithPath("posts").type(JsonFieldType.OBJECT).description("컬렉션 항목 페이지"))
            .and(
                CursorPaginationResponseSnippets.getCursorPaginationResponseFields("posts")
                    .getFieldDescriptors()
                    .toArray(FieldDescriptor[]::new));

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content",
            fieldWithPath(".collectionPostId")
                .type(JsonFieldType.NUMBER)
                .description("컬렉션 항목 ID (항목 제거 시 사용)"),
            fieldWithPath(".post")
                .type(JsonFieldType.OBJECT)
                .description(
                    "게시글 요약 (뷰어가 열람 가능한 경우에만 존재. 없으면 삭제·비공개로 열람 불가한 tombstone 이다. 본문 열람 수준은 post.accessLevel 로 구분한다)")
                .optional());

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content.post",
            PostSummarySnippets.getPostSummaryFields(".").stream()
                .map(FieldDescriptor::optional)
                .toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
