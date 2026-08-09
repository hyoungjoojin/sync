package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.post.dto.request.UpdatePostSeriesRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class UpdatePostSeriesRequestSnippets {

  public static UpdatePostSeriesRequest getUpdatePostSeriesRequest() {
    return new UpdatePostSeriesRequest("수정된 시리즈 이름");
  }

  public static RequestFieldsSnippet getUpdatePostSeriesRequestFields() {
    return requestFields(
        fieldWithPath("name")
            .type(JsonFieldType.STRING)
            .description("수정할 시리즈 이름 (null 이면 기존 이름 유지, 최대 255자)")
            .optional());
  }
}
