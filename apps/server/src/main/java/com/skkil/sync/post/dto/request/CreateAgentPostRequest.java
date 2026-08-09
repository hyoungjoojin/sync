package com.skkil.sync.post.dto.request;

import com.skkil.sync.post.model.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * 에이전트가 사용자를 대신해 초안을 만들 때 보내는 요청.
 *
 * <p>{@code status} 필드가 없다는 점이 중요하다. 에이전트가 만든 글은 언제나 초안이며, 발행은 사람이 SYNC 의 세션 인증 화면에서 직접 한다. 필드가 아예
 * 없으므로 "발행 상태로 만들어 달라"는 요청 자체를 표현할 수 없다.
 *
 * <p>{@code content}(Tiptap JSON) 필드도 없다. 에이전트는 Markdown 만 보내고, 서버는 그것을 해석하지 않는다.
 */
@Builder
public record CreateAgentPostRequest(
    @NotNull PostType type,
    @Nullable String title,
    @NotBlank String bodyMarkdown,
    @Nullable List<String> tags,
    /** {@code null} 이면 개인 글, 값이 있으면 해당 프로젝트의 글로 만든다. */
    @Nullable String projectHandle,
    /** {@code projectHandle} 이 없으면 무시된다. */
    @Nullable List<String> projectTags) {

  public List<String> tagsOrEmpty() {
    return tags == null ? List.of() : tags;
  }

  public List<String> projectTagsOrEmpty() {
    return projectHandle == null || projectTags == null ? List.of() : projectTags;
  }
}
