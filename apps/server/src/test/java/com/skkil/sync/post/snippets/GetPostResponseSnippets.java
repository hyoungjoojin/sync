package com.skkil.sync.post.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.post.dto.response.GetPostResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetPostResponseSnippets {

  public static GetPostResponse getGetPostResponse() {
    GetPostResponse.Media media =
        GetPostResponse.Media.builder()
            .id(1L)
            .url("https://example.com/media.png")
            .fileName("media.png")
            .fileSize(102400L)
            .mediaType("image/png")
            .build();

    GetPostResponse.Content content =
        GetPostResponse.TiptapContent.builder().json("Post Content").media(List.of(media)).build();

    return GetPostResponse.builder()
        .summary(PostSummarySnippets.getPostSummary())
        .content(content)
        .build();
  }

  public static ResponseFieldsSnippet getPostResponseFields() {
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("summary").type(JsonFieldType.OBJECT).description("포스트 정보"));
    fields.addAll(PostSummarySnippets.getPostSummaryFields("summary."));
    fields.add(
        fieldWithPath("content")
            .type(JsonFieldType.OBJECT)
            .description("게시글 본문. summary.accessLevel 이 PREVIEW(유료 게이트)이면 이 필드 자체가 응답에서 빠진다")
            .optional());
    fields.add(
        fieldWithPath("content.format")
            .type(JsonFieldType.STRING)
            .description(
                "본문 형식. TIPTAP_JSON 이면 content.json 이, MARKDOWN 이면 content.markdown 이 채워진다."
                    + " MARKDOWN 은 에이전트가 만든 미변환 초안에만 나타나며, 작성자가 에디터에서 처음 저장하면 TIPTAP_JSON 이 된다"));
    fields.add(
        fieldWithPath("content.json")
            .type(JsonFieldType.STRING)
            .description("Tiptap JSON 본문. format 이 TIPTAP_JSON 일 때만 존재한다")
            .optional());
    fields.add(
        fieldWithPath("content.markdown")
            .type(JsonFieldType.STRING)
            .description("Markdown 본문 원문. format 이 MARKDOWN 일 때만 존재한다")
            .optional());
    fields.add(
        fieldWithPath("content.media")
            .type(JsonFieldType.ARRAY)
            .description("Media attached to the post"));
    fields.add(
        fieldWithPath("content.media[].id").type(JsonFieldType.NUMBER).description("Media ID"));
    fields.add(
        fieldWithPath("content.media[].url").type(JsonFieldType.STRING).description("Media URL"));
    fields.add(
        fieldWithPath("content.media[].fileName")
            .type(JsonFieldType.STRING)
            .description("업로드된 원본 파일 이름"));
    fields.add(
        fieldWithPath("content.media[].fileSize")
            .type(JsonFieldType.NUMBER)
            .description("파일 크기 (바이트)"));
    fields.add(
        fieldWithPath("content.media[].mediaType")
            .type(JsonFieldType.STRING)
            .description("파일의 MIME 타입"));
    return responseFields(fields.toArray(FieldDescriptor[]::new));
  }
}
