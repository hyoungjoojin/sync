import type { OnboardedSession, Session } from './types';

export function isAuthenticated(session: Session | null): session is Session {
  return session !== null;
}

export function isOnboarded(
  session: Session | null,
): session is OnboardedSession {
  return (
    session !== null && session.user.isOnboarded && session.user.handle !== null
  );
}
