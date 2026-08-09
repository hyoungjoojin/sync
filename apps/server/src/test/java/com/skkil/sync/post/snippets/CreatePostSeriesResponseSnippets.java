package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.CreatePostSeriesResponse;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class CreatePostSeriesResponseSnippets {

  public static CreatePostSeriesResponse getCreatePostSeriesResponse() {
    return new CreatePostSeriesResponse("series-external-id");
  }

  public static ResponseFieldsSnippet getCreatePostSeriesResponseFields() {
    return responseFields(
        fieldWithPath("externalId").type(JsonFieldType.STRING).description("생성된 시리즈의 외부 식별자"));
  }
}
