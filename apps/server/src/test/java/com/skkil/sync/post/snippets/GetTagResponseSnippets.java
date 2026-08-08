package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.GetTagResponse;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetTagResponseSnippets {

  public static GetTagResponse getGetTagResponse() {
    return new GetTagResponse(TagSummarySnippets.getTagSummary());
  }

  public static ResponseFieldsSnippet getGetTagResponseFields() {
    return responseFields(
        TagSummarySnippets.getTagSummaryFields("tag.").toArray(new FieldDescriptor[0]));
  }
}
