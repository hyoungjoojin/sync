package com.skkil.sync.project.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.project.dto.response.GetProjectJoinRequestsResponse;
import com.skkil.sync.user.snippets.UserSummarySnippets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetProjectJoinRequestsResponseSnippets {

  public static GetProjectJoinRequestsResponse getGetProjectJoinRequestsResponse() {
    return new GetProjectJoinRequestsResponse(
        List.of(
            new GetProjectJoinRequestsResponse.JoinRequest(
                1L, UserSummarySnippets.getUserSummary(), Instant.parse("2026-07-09T00:00:00Z"))));
  }

  public static ResponseFieldsSnippet getGetProjectJoinRequestsResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("joinRequests").type(JsonFieldType.ARRAY).description("가입 요청 목록"));
    fields.add(
        fieldWithPath("joinRequests[].id").type(JsonFieldType.NUMBER).description("가입 요청 ID"));
    fields.add(
        fieldWithPath("joinRequests[].requester")
            .type(JsonFieldType.OBJECT)
            .description("가입 요청한 유저 정보"));
    fields.addAll(UserSummarySnippets.getUserSummaryFields("joinRequests[].requester."));
    fields.add(
        fieldWithPath("joinRequests[].createdAt")
            .type(JsonFieldType.STRING)
            .description("가입 요청 시각"));

    return responseFields(fields.toArray(FieldDescriptor[]::new));
  }
}
