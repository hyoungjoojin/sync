package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.OffsetPaginationResponseSnippets;
import com.skkil.sync.post.dto.response.GetFollowedTagsResponse;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetFollowedTagsResponseSnippets {

  public static GetFollowedTagsResponse getGetFollowedTagsResponse() {
    return new GetFollowedTagsResponse(
        OffsetPaginationResponseSnippets.of(GetTagsResponseSnippets.getGetTagsResponse().tags()));
  }

  public static ResponseFieldsSnippet getGetFollowedTagsResponseFields() {
    FieldDescriptors fields = OffsetPaginationResponseSnippets.getPaginationResponseFields("tags");

    fields =
        fields.andWithPrefix(
            "tags.content[]",
            TagSummarySnippets.getTagSummaryFields(".").toArray(FieldDescriptor[]::new));

    return responseFields(fields.getFieldDescriptors());
  }
}
