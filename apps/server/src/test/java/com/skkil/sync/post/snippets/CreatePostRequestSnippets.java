package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.post.dto.request.CreatePostRequest;
import com.skkil.sync.post.dto.request.PostContentRequest;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import java.util.List;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class CreatePostRequestSnippets {

  public static CreatePostRequest getCreatePostRequest() {
    PostContentRequest content =
        new PostContentRequest(
            "This is a post content.", "{\"text\": \"This is a post content.\"}", List.of(1L, 2L));

    return CreatePostRequest.builder()
        .title("title")
        .type(PostType.SHORT)
        .status(PostStatus.PUBLISHED)
        .content(content)
        .tags(List.of("java", "spring"))
        .referencedPostIds(List.of(10L, 11L))
        .coverMediaId("100")
        .build();
  }

  public static RequestFieldsSnippet getCreatePostRequestFields() {
    return requestFields(
        fieldWithPath("title").type(JsonFieldType.STRING).description("Title").optional(),
        fieldWithPath("type")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("Post Type")
            .attributes(RestDocsUtils.getEnumAttributes(PostType.class)),
        fieldWithPath("status")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("Post Status")
            .attributes(RestDocsUtils.getEnumAttributes(PostStatus.class))
            .optional(),
        fieldWithPath("content").type(JsonFieldType.OBJECT).description("Content"),
        fieldWithPath("content.text").type(JsonFieldType.STRING).description("Text Content"),
        fieldWithPath("content.json").type(JsonFieldType.STRING).description("JSON Content"),
        fieldWithPath("content.mediaIds")
            .type(JsonFieldType.ARRAY)
            .description("사용된 미디어 ID 목록")
            .optional(),
        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("태그 목록").optional(),
        fieldWithPath("referencedPostIds")
            .type(JsonFieldType.ARRAY)
            .description("이 글이 참조하는 게시글 ID 목록 (지정한 순서대로 노출, 최대 50개)")
            .optional(),
        fieldWithPath("coverMediaId")
            .type(JsonFieldType.STRING)
            .description("커버 이미지로 사용할 미디어 ID (없으면 커버 없음)")
            .optional());
  }
}
