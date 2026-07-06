package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.CursorPaginationResponseSnippets;
import com.skkil.sync.post.dto.response.GetPostsResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetPostsResponseSnippets {

  public static GetPostsResponse getGetPostsResponse() {
    GetPostsResponse.Post post =
        GetPostsResponse.Post.builder()
            .summary(PostSummarySnippets.getPostSummary())
            .content("Post Content")
            .build();

    return new GetPostsResponse(CursorPaginationResponseSnippets.of(List.of(post)));
  }

  public static ResponseFieldsSnippet getPostsResponseFields() {
    FieldDescriptors fields =
        CursorPaginationResponseSnippets.getCursorPaginationResponseFields("posts");

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content",
            fieldWithPath(".summary").type(JsonFieldType.OBJECT).description("Post Summary"),
            fieldWithPath(".content").type(JsonFieldType.STRING).description("Post Content"));

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content.summary",
            PostSummarySnippets.getPostSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }

  public static GetPostsResponse getGetBookmarkedPostsResponse() {
    GetPostsResponse.Post post =
        GetPostsResponse.Post.builder()
            .summary(PostSummarySnippets.getPostSummary())
            .content("This is a bookmarked post content")
            .bookmarked(true)
            .bookmarkedAt(OffsetDateTime.of(2026, 5, 5, 12, 0, 0, 0, ZoneOffset.UTC))
            .build();

    return new GetPostsResponse(CursorPaginationResponseSnippets.of(List.of(post)));
  }

  public static ResponseFieldsSnippet getBookmarkedPostsResponseFields() {
    FieldDescriptors fields =
        CursorPaginationResponseSnippets.getCursorPaginationResponseFields("posts");

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content",
            fieldWithPath(".summary").type(JsonFieldType.OBJECT).description("Post Summary"),
            fieldWithPath(".content").type(JsonFieldType.STRING).description("Content"),
            fieldWithPath(".bookmarked")
                .type(JsonFieldType.BOOLEAN)
                .description("Whether the current user bookmarked this post"),
            fieldWithPath(".bookmarkedAt").type(JsonFieldType.STRING).description("Bookmarked At"));

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content.summary",
            PostSummarySnippets.getPostSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
