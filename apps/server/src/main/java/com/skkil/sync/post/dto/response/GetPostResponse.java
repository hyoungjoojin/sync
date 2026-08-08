package com.skkil.sync.post.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.model.PostContentFormat;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetPostResponse(PostSummary summary, Content content) {

  /**
   * 본문 페이로드. 형식별로 필드 이름이 다른 별개의 타입이므로, {@code format} 이 TIPTAP_JSON 이라고 말하면서 실제로는 markdown 필드를 담은
   * 응답을 만들 수 없다. 형식은 저장된 컬럼이 아니라 {@link com.skkil.sync.post.model.Post} 의 두 본문 컬럼 중 어느 쪽이 채워져 있는지에서
   * 파생된다.
   */
  // 판별자는 Jackson 이 직접 써 넣는다(As.PROPERTY). 레코드는 컴포넌트가 아닌 접근자를 직렬화하지
  // 않으므로 EXISTING_PROPERTY 로는 format 이 페이로드에 나타나지 않는다. 아래 서브타입 이름은
  // PostContentFormat 의 상수 이름과 같아서, JSON 의 format 값과 자바 쪽 format() 이 어긋날 수 없다.
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "format")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = TiptapContent.class, name = "TIPTAP_JSON"),
    @JsonSubTypes.Type(value = MarkdownContent.class, name = "MARKDOWN")
  })
  public sealed interface Content permits TiptapContent, MarkdownContent {
    PostContentFormat format();

    List<Media> media();
  }

  /** 에디터가 저장한 Tiptap JSON 본문. */
  @Builder
  public record TiptapContent(String json, List<Media> media) implements Content {
    @Override
    public PostContentFormat format() {
      return PostContentFormat.TIPTAP_JSON;
    }
  }

  /** 에이전트가 보낸 Markdown 원문. 서버는 이 값을 해석하지 않고 그대로 전달하며, HTML 변환은 브라우저의 에디터가 담당한다. */
  @Builder
  public record MarkdownContent(String markdown, List<Media> media) implements Content {
    @Override
    public PostContentFormat format() {
      return PostContentFormat.MARKDOWN;
    }
  }

  @Builder
  public static record Media(
      Long id, String url, String fileName, Long fileSize, String mediaType) {}
}
