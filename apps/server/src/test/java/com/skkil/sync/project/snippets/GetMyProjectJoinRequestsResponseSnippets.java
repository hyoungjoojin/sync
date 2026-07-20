package com.skkil.sync.project.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.project.dto.response.GetMyProjectJoinRequestsResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetMyProjectJoinRequestsResponseSnippets {

  public static GetMyProjectJoinRequestsResponse getGetMyProjectJoinRequestsResponse() {
    return new GetMyProjectJoinRequestsResponse(
        List.of(
            new GetMyProjectJoinRequestsResponse.JoinRequest(
                1L,
                ProjectSummarySnippets.getProjectSummary(),
                Instant.parse("2026-07-09T00:00:00Z"))));
  }

  public static ResponseFieldsSnippet getGetMyProjectJoinRequestsResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("joinRequests").type(JsonFieldType.ARRAY).description("가입 요청 목록"));
    fields.add(
        fieldWithPath("joinRequests[].id").type(JsonFieldType.NUMBER).description("가입 요청 ID"));
    fields.add(
        fieldWithPath("joinRequests[].project").type(JsonFieldType.OBJECT).description("프로젝트 정보"));
    fields.addAll(ProjectSummarySnippets.getProjectSummaryFields("joinRequests[].project."));
    fields.add(
        fieldWithPath("joinRequests[].createdAt")
            .type(JsonFieldType.STRING)
            .description("가입 요청 시각"));

    return responseFields(fields.toArray(FieldDescriptor[]::new));
  }
}
