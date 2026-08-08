package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.OffsetPaginationResponseSnippets;
import com.skkil.sync.post.dto.response.GetAllTagsResponse;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetAllTagsResponseSnippets {

  public static GetAllTagsResponse getGetAllTagsResponse() {
    return new GetAllTagsResponse(
        OffsetPaginationResponseSnippets.of(GetTagsResponseSnippets.getGetTagsResponse().tags()));
  }

  public static ResponseFieldsSnippet getGetAllTagsResponseFields() {
    FieldDescriptors fields = OffsetPaginationResponseSnippets.getPaginationResponseFields("tags");

    fields =
        fields.andWithPrefix(
            "tags.content[]",
            TagSummarySnippets.getTagSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
