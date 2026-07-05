package com.skkil.sync.user.dto.response;

import java.util.List;
import lombok.Builder;

public record GetUserRecommendationsResponse(List<User> users) {

  @Builder
  public record User(String userId, String handle, String name, String profileImageUrl) {}
}
