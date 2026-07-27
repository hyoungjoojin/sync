package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.CursorPaginationResponseSnippets;
import com.skkil.sync.post.dto.response.PaginatedGetPostsResponse;
import com.skkil.sync.post.model.PostStatus;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

/**
 * 커서 페이지네이션이 붙은 게시글 목록 응답({@link PaginatedGetPostsResponse})의 공통 스니펫. 피드·초안·북마크·좋아요·추천 등 모든 페이지네이션
 * 목록 엔드포인트가 같은 스키마를 공유하므로 필드 스니펫은 하나만 둔다. 예시 객체만 게시글 상태(발행/초안)에 따라 나뉜다.
 */
public class PaginatedGetPostsResponseSnippets {

  public static PaginatedGetPostsResponse getPaginatedGetPostsResponse() {
    return getPaginatedGetPostsResponse(PostStatus.PUBLISHED);
  }

  public static PaginatedGetPostsResponse getGetDraftPostsResponse() {
    return getPaginatedGetPostsResponse(PostStatus.DRAFT);
  }

  private static PaginatedGetPostsResponse getPaginatedGetPostsResponse(PostStatus status) {
    return new PaginatedGetPostsResponse(
        CursorPaginationResponseSnippets.of(List.of(PostSummarySnippets.getPostSummary(status))));
  }

  public static ResponseFieldsSnippet getPostsResponseFields() {
    FieldDescriptors fields =
        CursorPaginationResponseSnippets.getCursorPaginationResponseFields("posts");

    fields =
        fields.andWithPrefix(
            "posts.nodes[].content",
            PostSummarySnippets.getPostSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
