package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.post.dto.request.CreatePostSeriesRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class CreatePostSeriesRequestSnippets {

  public static CreatePostSeriesRequest getCreatePostSeriesRequest() {
    return new CreatePostSeriesRequest("시리즈 이름");
  }

  public static RequestFieldsSnippet getCreatePostSeriesRequestFields() {
    return requestFields(
        fieldWithPath("name").type(JsonFieldType.STRING).description("시리즈 이름 (최대 255자)"));
  }
}
