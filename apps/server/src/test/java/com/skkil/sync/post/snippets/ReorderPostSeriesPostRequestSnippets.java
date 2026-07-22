package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.post.dto.request.ReorderPostSeriesPostRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class ReorderPostSeriesPostRequestSnippets {

  public static ReorderPostSeriesPostRequest getReorderPostSeriesPostRequest() {
    return new ReorderPostSeriesPostRequest(2);
  }

  public static RequestFieldsSnippet getReorderPostSeriesPostRequestFields() {
    return requestFields(
        fieldWithPath("position").type(JsonFieldType.NUMBER).description("이동할 새 위치 (1부터 시작)"));
  }
}
