package com.skkil.sync.project.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.project.dto.summary.AdminProjectSummary;
import com.skkil.sync.project.model.JoinPolicy;
import com.skkil.sync.user.snippets.UserSummarySnippets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class AdminProjectSummarySnippets {

  public static AdminProjectSummary getAdminProjectSummary() {
    return AdminProjectSummary.builder()
        .id(1L)
        .handle("my-project")
        .name("나의 프로젝트")
        .description("프로젝트 설명")
        .website("https://example.com")
        .isPublic(true)
        .joinPolicy(JoinPolicy.INVITE)
        .followerCount(42)
        .teammateCount(8)
        .owner(UserSummarySnippets.getUserSummary())
        .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
        .iconUrl("https://example.com/icon.png")
        .build();
  }

  public static ResponseFieldsSnippet getAdminProjectSummaryResponseFields() {
    return responseFields(getAdminProjectSummaryFields("").toArray(new FieldDescriptor[0]));
  }

  public static List<FieldDescriptor> getAdminProjectSummaryFields(String prefix) {
    List<FieldDescriptor> fields = new ArrayList<>();

    fields.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("프로젝트 ID"));
    fields.add(fieldWithPath(prefix + "handle").type(JsonFieldType.STRING).description("프로젝트 핸들"));
    fields.add(fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("프로젝트 이름"));
    fields.add(
        fieldWithPath(prefix + "description")
            .type(JsonFieldType.STRING)
            .optional()
            .description("프로젝트 설명"));
    fields.add(
        fieldWithPath(prefix + "website")
            .type(JsonFieldType.STRING)
            .optional()
            .description("프로젝트 웹사이트"));
    fields.add(fieldWithPath(prefix + "isPublic").type(JsonFieldType.BOOLEAN).description("공개 여부"));
    fields.add(
        fieldWithPath(prefix + "joinPolicy")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("프로젝트 참여 정책")
            .attributes(RestDocsUtils.getEnumAttributes(JoinPolicy.class)));
    fields.add(
        fieldWithPath(prefix + "followerCount").type(JsonFieldType.NUMBER).description("팔로워 수"));
    fields.add(
        fieldWithPath(prefix + "teammateCount").type(JsonFieldType.NUMBER).description("팀원 수"));
    fields.add(
        fieldWithPath(prefix + "owner")
            .type(JsonFieldType.OBJECT)
            .optional()
            .description("프로젝트 소유자"));
    fields.addAll(UserSummarySnippets.getUserSummaryFields(prefix + "owner."));
    fields.add(fieldWithPath(prefix + "createdAt").type(JsonFieldType.STRING).description("생성 시각"));
    fields.add(
        fieldWithPath(prefix + "iconUrl")
            .type(JsonFieldType.STRING)
            .optional()
            .description("프로젝트 아이콘 URL"));

    return fields;
  }
}
