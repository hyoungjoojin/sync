import type { auth } from './index';

// auth 인스턴스는 서버 전용(BETTER_AUTH_SECRET 필요)이므로 값으로 import하면
// 이 모듈을 쓰는 클라이언트 컴포넌트 번들에 betterAuth()가 끌려 들어가
// 브라우저에서 default secret 에러가 난다. 순수 헬퍼는 타입만 참조한다.
type Session = Awaited<ReturnType<typeof auth.api.getSession>>;

export function isAuthenticated(
  session: Session,
): session is NonNullable<Session> {
  return session !== null && session.user !== null;
}

export function isOnboarded(session: Session) {
  return session !== null && session.user !== null && session.user.isOnboarded;
}
