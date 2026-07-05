package com.skkil.sync.post.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.skkil.sync.post.dto.summary.PostSummary;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetPostResponse(
    PostSummary summary, Content content, Long likeCount, Long commentCount, boolean bookmarked) {

  @Builder
  public static record Content(String json, List<Media> media) {}

  @Builder
  public static record Media(Long id, String url) {}
}
