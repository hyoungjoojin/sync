package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.post.dto.request.AddPostToPostSeriesRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class AddPostToPostSeriesRequestSnippets {

  public static AddPostToPostSeriesRequest getAddPostToPostSeriesRequest() {
    return new AddPostToPostSeriesRequest("project-handle", "post-slug", 1);
  }

  public static RequestFieldsSnippet getAddPostToPostSeriesRequestFields() {
    return requestFields(
        fieldWithPath("projectHandle")
            .type(JsonFieldType.STRING)
            .description("게시글이 속한 프로젝트 핸들 (개인 게시글인 경우 없음)")
            .optional(),
        fieldWithPath("postHandle")
            .type(JsonFieldType.STRING)
            .description("시리즈에 추가할 게시글의 핸들(slug)"),
        fieldWithPath("position")
            .type(JsonFieldType.NUMBER)
            .description("추가할 위치 (지정하지 않으면 맨 뒤에 추가)")
            .optional());
  }
}
