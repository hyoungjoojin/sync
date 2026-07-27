package com.skkil.sync.collection.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.model.CollectionScope;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetCollectionsResponseSnippets {

  public static GetCollectionsResponse getGetCollectionsResponse() {
    CollectionSummary personal = CollectionSummarySnippets.getCollectionSummary();
    CollectionSummary workspace =
        new CollectionSummary(
            "team-picks-e5f6g7h8",
            "Team Picks",
            null,
            CollectionScope.WORKSPACE,
            true,
            5L,
            1L,
            "my-project",
            true);

    return new GetCollectionsResponse(List.of(personal, workspace));
  }

  public static ResponseFieldsSnippet getGetCollectionsResponseFields() {
    List<FieldDescriptor> fields = new java.util.ArrayList<>();
    fields.add(fieldWithPath("collections").type(JsonFieldType.ARRAY).description("컬렉션 목록"));
    fields.addAll(CollectionSummarySnippets.getCollectionSummaryFields("collections[]."));
    return responseFields(fields);
  }
}
