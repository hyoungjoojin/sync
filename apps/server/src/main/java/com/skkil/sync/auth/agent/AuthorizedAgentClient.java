package com.skkil.sync.auth.agent;

/**
 * 요청을 보낸 에이전트 OAuth2 클라이언트의 신원. 검증이 끝난 액세스 토큰의 {@code client_id} 클레임에서 나오며, 요청 본문에서 오는 값이 아니다.
 *
 * <p>{@code registeredClientId} 는 {@code oauth2_registered_client.id}(등록 행의 기본키)다. OAuth 의 {@code
 * client_id} 문자열이 아니라 이 값을 쓰는 이유는 {@code posts.created_via_client_id} 의 외래키가 기본키를 가리키기 때문이다.
 *
 * <p>배지("{clientName} 으로 작성됨")와 출처 기록에만 쓰인다. 권한 판단에는 관여하지 않는다 — 무엇을 할 수 있는지는 토큰의 스코프와 {@code sub}
 * 클레임이 가리키는 사용자가 정한다.
 */
public record AuthorizedAgentClient(String registeredClientId, String clientName) {}
