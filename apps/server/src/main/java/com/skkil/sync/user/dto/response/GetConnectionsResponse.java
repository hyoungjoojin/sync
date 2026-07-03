package com.skkil.sync.user.dto.response;

import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;

public record GetConnectionsResponse(CursorPaginationResponse<Connection> connections) {

  public static record Connection(String userId, String handle, String name) {}
}
