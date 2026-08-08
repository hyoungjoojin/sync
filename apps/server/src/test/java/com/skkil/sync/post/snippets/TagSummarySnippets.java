package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.skkil.sync.post.dto.summary.TagSummary;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class TagSummarySnippets {

  public static TagSummary getTagSummary() {
    return TagSummary.builder()
        .id(1L)
        .name("java")
        .description("자바 관련 태그")
        .postCount(10L)
        .followerCount(3L)
        .isFollowing(false)
        .verified(true)
        .build();
  }

  public static List<FieldDescriptor> getTagSummaryFields(String prefix) {
    return List.of(
        fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("태그 ID"),
        fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("태그 이름"),
        fieldWithPath(prefix + "description").type(JsonFieldType.STRING).description("태그 설명"),
        fieldWithPath(prefix + "postCount").type(JsonFieldType.NUMBER).description("태그가 사용된 게시물 수"),
        fieldWithPath(prefix + "followerCount")
            .type(JsonFieldType.NUMBER)
            .description("태그를 팔로우하는 사용자 수"),
        fieldWithPath(prefix + "projectHandle")
            .type(JsonFieldType.STRING)
            .description("프로젝트 태그인 경우 해당 프로젝트의 핸들 (전역 태그인 경우 없음)")
            .optional(),
        fieldWithPath(prefix + "isFollowing")
            .type(JsonFieldType.BOOLEAN)
            .description("요청자가 해당 태그를 팔로우하고 있는지 여부 (프로젝트 태그는 항상 false)"),
        fieldWithPath(prefix + "verified").type(JsonFieldType.BOOLEAN).description("태그 검증 여부"));
  }
}
