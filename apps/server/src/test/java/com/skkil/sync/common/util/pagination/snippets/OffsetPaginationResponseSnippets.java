package com.skkil.sync.common.util.pagination.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.dto.response.OffsetPaginationResponse;
import java.util.List;
import org.springframework.restdocs.payload.JsonFieldType;

public class OffsetPaginationResponseSnippets {

  public static <T> OffsetPaginationResponse<T> of(List<T> content) {
    OffsetPaginationResponse.PageInfo pageInfo =
        OffsetPaginationResponse.PageInfo.builder()
            .page(1)
            .size(1)
            .hasNextPage(true)
            .hasPreviousPage(true)
            .build();

    return new OffsetPaginationResponse<>(pageInfo, content);
  }

  /**
   * 래퍼 객체 자체({@code pathPrefix})를 필드로 선언해야 스키마의 {@code required}에 포함된다. 이것이 빠지면 서버가 항상 채워 보내는 값인데도
   * 생성 타입이 optional로 내려가 호출부마다 불필요한 null 검사가 생긴다.
   */
  public static FieldDescriptors getPaginationResponseFields(String pathPrefix) {
    FieldDescriptors fields =
        new FieldDescriptors(
            fieldWithPath(pathPrefix).type(JsonFieldType.OBJECT).description("Paginated Result"));

    return fields.andWithPrefix(
        pathPrefix,
        fieldWithPath(".pageInfo").type(JsonFieldType.OBJECT).description("Page Info").attributes(),
        fieldWithPath(".pageInfo.page").type(JsonFieldType.NUMBER).description("Page Number"),
        fieldWithPath(".pageInfo.size").type(JsonFieldType.NUMBER).description("Page Size"),
        fieldWithPath(".pageInfo.hasNextPage")
            .type(JsonFieldType.BOOLEAN)
            .description("Has Next Page"),
        fieldWithPath(".pageInfo.hasPreviousPage")
            .type(JsonFieldType.BOOLEAN)
            .description("Has Previous Page"),
        fieldWithPath(".content").type(JsonFieldType.ARRAY).description("Content"));
  }
}
