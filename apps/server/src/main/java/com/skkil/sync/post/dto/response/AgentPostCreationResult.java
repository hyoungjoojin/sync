package com.skkil.sync.post.dto.response;

import com.skkil.sync.post.model.PostType;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * 에이전트가 초안을 만든 결과. {@code reviewUrl} 은 상세 화면이 아니라 편집 화면을 가리킨다. 초안은 아직 Markdown 상태이고, 그것을 Tiptap 으로
 * 바꾸는 것은 편집 화면의 에디터가 열릴 때이기 때문이다. 에이전트는 이 링크를 사람에게 그대로 보여주면 된다.
 */
@Builder
public record AgentPostCreationResult(
    String slug, String reviewUrl, PostType type, @Nullable String title, Instant createdAt) {}
