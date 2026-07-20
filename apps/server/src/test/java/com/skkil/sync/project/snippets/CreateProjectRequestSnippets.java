package com.skkil.sync.project.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.project.dto.request.CreateProjectRequest;
import com.skkil.sync.project.model.JoinPolicy;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class CreateProjectRequestSnippets {

  public static CreateProjectRequest getCreateProjectRequest() {
    return new CreateProjectRequest("project-handle", "새로운 프로젝트", "프로젝트 설명", true, JoinPolicy.OPEN);
  }

  public static RequestFieldsSnippet getCreateProjectRequestFields() {
    return requestFields(
        fieldWithPath("handle").type(JsonFieldType.STRING).description("프로젝트 핸들"),
        fieldWithPath("name").type(JsonFieldType.STRING).description("프로젝트 이름"),
        fieldWithPath("description").type(JsonFieldType.STRING).description("프로젝트 설명").optional(),
        fieldWithPath("isPublic").type(JsonFieldType.BOOLEAN).description("공개 여부"),
        fieldWithPath("joinPolicy")
            .type(RestDocsUtils.ENUM_TYPE)
            .optional()
            .description("프로젝트 참여 정책 (미지정 시 INVITE)")
            .attributes(RestDocsUtils.getEnumAttributes(JoinPolicy.class)));
  }
}
