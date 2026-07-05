package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.common.util.time.DateTimeTestUtils;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.project.snippets.ProjectSummarySnippets;
import com.skkil.sync.user.snippets.UserSummarySnippets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class PostSummarySnippets {

  public static PostSummary getPostSummary() {
    return PostSummary.builder()
        .id(1L)
        .slug("test-slug")
        .type(PostType.SHORT)
        .author(UserSummarySnippets.getUserSummary())
        .project(ProjectSummarySnippets.getProjectSummary())
        .resolved(false)
        .createdAt(DateTimeTestUtils.defaultTestOffsetDateTime())
        .build();
  }

  public static List<FieldDescriptor> getPostSummaryFields(String prefix) {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("Post ID"));
    fields.add(fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).description("Post Slug"));
    fields.add(
        fieldWithPath(prefix + "type")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("Post Type")
            .attributes(RestDocsUtils.getEnumAttributes(PostType.class)));
    fields.add(fieldWithPath(prefix + "author").type(JsonFieldType.OBJECT).description("작성자 정보"));
    fields.addAll(UserSummarySnippets.getUserSummaryFields(prefix + "author."));
    fields.add(
        fieldWithPath(prefix + "project")
            .type(JsonFieldType.OBJECT)
            .description("소속 프로젝트 정보")
            .optional());
    ProjectSummarySnippets.getProjectSummaryFields(prefix + "project.").stream()
        .map(FieldDescriptor::optional)
        .forEach(fields::add);
    fields.add(
        fieldWithPath(prefix + "resolved")
            .type(JsonFieldType.BOOLEAN)
            .description("Whether the question post has been resolved"));
    fields.add(
        fieldWithPath(prefix + "createdAt")
            .type(JsonFieldType.STRING)
            .description("Creation Timestamp"));
    return fields;
  }
}
