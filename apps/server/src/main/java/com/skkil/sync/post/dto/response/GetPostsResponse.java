package com.skkil.sync.post.dto.response;

import com.skkil.sync.post.dto.summary.PostSummary;
import java.util.List;

/**
 * 페이지네이션 없이 게시글 요약 목록만 돌려주는 공통 응답. 검색 결과·관련 게시글·참조 목록처럼 서버가 개수를 정하는(클라이언트가 더 요청할 수 없는) 목록은 모두 이 타입을
 * 쓴다. 커서 페이지네이션이 붙는 목록은 {@link PaginatedGetPostsResponse} 다.
 */
public record GetPostsResponse(List<PostSummary> posts) {}
