package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.collection.dto.request.CreateCollectionRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class CreateCollectionRequestSnippets {

  public static CreateCollectionRequest getCreateCollectionRequest() {
    return new CreateCollectionRequest("My Collection", "A collection of posts", true);
  }

  public static RequestFieldsSnippet getCreateCollectionRequestFields() {
    return requestFields(
        fieldWithPath("name").type(JsonFieldType.STRING).description("컬렉션 이름 (최대 255자)"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("컬렉션 설명").optional(),
        fieldWithPath("isPublic")
            .type(JsonFieldType.BOOLEAN)
            .description("공개 여부 (생략 시 공개)")
            .optional());
  }
}
