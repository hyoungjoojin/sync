package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.collection.dto.request.UpdateCollectionRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class UpdateCollectionRequestSnippets {

  public static UpdateCollectionRequest getUpdateCollectionRequest() {
    return new UpdateCollectionRequest("Updated Collection", "Updated description", false);
  }

  public static RequestFieldsSnippet getUpdateCollectionRequestFields() {
    return requestFields(
        fieldWithPath("name")
            .type(JsonFieldType.STRING)
            .description("변경할 컬렉션 이름 (생략 시 기존 값 유지, 최대 255자)")
            .optional(),
        fieldWithPath("description")
            .type(JsonFieldType.STRING)
            .description("변경할 컬렉션 설명 (생략 시 기존 값 유지)")
            .optional(),
        fieldWithPath("isPublic")
            .type(JsonFieldType.BOOLEAN)
            .description("변경할 공개 여부 (생략 시 기존 값 유지)")
            .optional());
  }
}
