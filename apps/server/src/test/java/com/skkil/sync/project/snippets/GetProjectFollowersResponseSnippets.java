package com.skkil.sync.project.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.CursorPaginationResponseSnippets;
import com.skkil.sync.project.dto.response.GetProjectFollowersResponse;
import java.util.List;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetProjectFollowersResponseSnippets {

  public static GetProjectFollowersResponse getGetProjectFollowersResponse() {
    GetProjectFollowersResponse.Follower follower =
        new GetProjectFollowersResponse.Follower("1", "user-handle", "User");

    return new GetProjectFollowersResponse(CursorPaginationResponseSnippets.of(List.of(follower)));
  }

  public static ResponseFieldsSnippet getProjectFollowersResponseFields() {
    FieldDescriptors fields =
        CursorPaginationResponseSnippets.getCursorPaginationResponseFields("followers");

    fields =
        fields.andWithPrefix(
            "followers.nodes[].content",
            fieldWithPath(".userId").type(JsonFieldType.STRING).description("User ID"),
            fieldWithPath(".handle").type(JsonFieldType.STRING).description("User Handle"),
            fieldWithPath(".name").type(JsonFieldType.STRING).description("User Name"));

    return responseFields(fields.getFieldDescriptors());
  }
}
