package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.GetTagsResponse;
import com.skkil.sync.post.dto.summary.TagSummary;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetTagsResponseSnippets {

  public static GetTagsResponse getGetTagsResponse() {
    return new GetTagsResponse(
        List.of(
            TagSummarySnippets.getTagSummary(),
            TagSummary.builder()
                .id(2L)
                .name("spring")
                .description("스프링 프레임워크 관련 태그")
                .postCount(5L)
                .followerCount(1L)
                .isFollowing(true)
                .verified(false)
                .build()));
  }

  public static ResponseFieldsSnippet getGetTagsResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("tags").type(JsonFieldType.ARRAY).description("태그 목록"));
    fields.addAll(TagSummarySnippets.getTagSummaryFields("tags[]."));

    return responseFields(fields.toArray(new FieldDescriptor[0]));
  }
}
