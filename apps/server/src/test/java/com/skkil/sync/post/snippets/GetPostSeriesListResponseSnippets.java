package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.GetPostSeriesListResponse;
import com.skkil.sync.post.dto.summary.PostSeriesSummary;
import com.skkil.sync.post.model.PostScope;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetPostSeriesListResponseSnippets {

  public static GetPostSeriesListResponse getGetPostSeriesListResponse() {
    PostSeriesSummary personal =
        new PostSeriesSummary("getting-started", "시작하기", PostScope.PUBLIC, 2L, 1L, null);
    PostSeriesSummary workspace =
        new PostSeriesSummary(
            "series-external-id", "시리즈 이름", PostScope.WORKSPACE, 3L, 1L, "project-handle");
    return new GetPostSeriesListResponse(List.of(personal, workspace));
  }

  public static ResponseFieldsSnippet getGetPostSeriesListResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("series").type(JsonFieldType.ARRAY).description("시리즈 목록"));
    fields.addAll(PostSeriesSummarySnippets.getPostSeriesSummaryFields("series[]."));
    return responseFields(fields);
  }
}
