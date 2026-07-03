package com.skkil.sync.project.dto.response;

import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;

public record GetProjectFollowersResponse(CursorPaginationResponse<Follower> followers) {

  public static record Follower(String userId, String handle, String name) {}
}
