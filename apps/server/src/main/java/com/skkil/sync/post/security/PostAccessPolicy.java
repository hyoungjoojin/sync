package com.skkil.sync.post.security;

import com.skkil.sync.post.dto.data.PostDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PostAccessPolicy {

  public Map<Long, PostAccessLevel> resolveAccessLevels(
      @Nullable Long requesterId, List<PostDto> posts) {
    return posts.stream()
        .collect(Collectors.toMap(PostDto::id, post -> PostAccessLevel.FULL, (a, b) -> a));
  }
}
