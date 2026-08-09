package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.collection.dto.response.CreateCollectionResponse;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class CreateCollectionResponseSnippets {

  public static CreateCollectionResponse getCreateCollectionResponse() {
    return new CreateCollectionResponse("my-collection-a1b2c3d4");
  }

  public static ResponseFieldsSnippet getCreateCollectionResponseFields() {
    return responseFields(
        fieldWithPath("externalId")
            .type(JsonFieldType.STRING)
            .description("생성된 컬렉션의 외부 식별자 (이후 모든 API 가 이 값으로 컬렉션을 참조한다)"));
  }
}
