package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.GetPostSeriesResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetPostSeriesResponseSnippets {

  public static GetPostSeriesResponse getGetPostSeriesResponse() {
    return new GetPostSeriesResponse(
        PostSeriesSummarySnippets.getPostSeriesSummary(),
        1L,
        List.of(new GetPostSeriesResponse.Post(1L, 1, "게시글 제목")));
  }

  public static ResponseFieldsSnippet getGetPostSeriesResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(
        fieldWithPath("series")
            .type(JsonFieldType.OBJECT)
            .description("게시글이 속한 시리즈 (어떤 시리즈에도 속하지 않으면 없음)")
            .optional());
    // 시리즈가 없을 수 있으므로 하위 필드는 모두 optional 로 문서화한다.
    PostSeriesSummarySnippets.getPostSeriesSummaryFields("series.").stream()
        .map(FieldDescriptor::optional)
        .forEach(fields::add);
    fields.add(
        fieldWithPath("currentSeriesPostId")
            .type(JsonFieldType.NUMBER)
            .description("조회 기준 게시글의 시리즈-게시글 연결 ID (시리즈에 속하지 않으면 없음)")
            .optional());
    fields.add(
        fieldWithPath("posts")
            .type(JsonFieldType.ARRAY)
            .description("시리즈에 포함된 게시글 목록 (순서대로, 시리즈에 속하지 않으면 빈 배열)"));
    fields.add(
        fieldWithPath("posts[].seriesPostId")
            .type(JsonFieldType.NUMBER)
            .description("시리즈-게시글 연결 ID (순서 변경/삭제 시 사용)"));
    fields.add(
        fieldWithPath("posts[].position")
            .type(JsonFieldType.NUMBER)
            .description("시리즈 내 게시글 위치 (1부터 시작)"));
    fields.add(
        fieldWithPath("posts[].title")
            .type(JsonFieldType.STRING)
            .description("게시글 제목 (열람 권한이 없거나 삭제된 경우 없음)")
            .optional());
    return responseFields(fields);
  }
}
