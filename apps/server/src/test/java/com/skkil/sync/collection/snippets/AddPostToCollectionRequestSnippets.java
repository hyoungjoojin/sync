package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.collection.dto.request.AddPostToCollectionRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class AddPostToCollectionRequestSnippets {

  public static AddPostToCollectionRequest getAddPostToCollectionRequest() {
    return new AddPostToCollectionRequest("my-project", "some-post-slug");
  }

  public static RequestFieldsSnippet getAddPostToCollectionRequestFields() {
    return requestFields(
        fieldWithPath("projectHandle")
            .type(JsonFieldType.STRING)
            .description("게시글이 속한 프로젝트의 핸들 (개인 게시글이면 생략)")
            .optional(),
        fieldWithPath("postHandle")
            .type(JsonFieldType.STRING)
            .description("게시글의 전역 유니크 식별자 (slug)"));
  }
}
