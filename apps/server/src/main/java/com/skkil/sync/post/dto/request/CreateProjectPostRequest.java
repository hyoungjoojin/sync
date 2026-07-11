package com.skkil.sync.post.dto.request;

import com.skkil.sync.post.constants.PostConstants;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateProjectPostRequest(
    String title,
    @NotNull PostType type,
    PostStatus status,
    @Valid @NotNull Content content,
    List<String> tags,
    List<String> projectTags) {

  public static record Content(
      @NotBlank @Size(max = PostConstants.MAX_CONTENT_TEXT_LENGTH) String text,
      @NotBlank @Size(max = PostConstants.MAX_CONTENT_JSON_LENGTH) String json,
      List<Long> mediaIds) {}
}
