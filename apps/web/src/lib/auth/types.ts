import type { GetProfileResponse } from '@/api/__generated__/types/GetProfileResponse';

export interface SessionUser {
  id: string;
  email: string;
  name: string;
  handle: string | null;
  image: string;
  isOnboarded: boolean;
  role: string;
}

export interface Session {
  user: SessionUser;
}

export interface OnboardedSession extends Session {
  user: SessionUser & { handle: string };
}

export function toSession(profile: GetProfileResponse): Session {
  return {
    user: {
      id: profile.userId,
      email: profile.email,
      name: profile.name,
      handle: profile.handle ?? null,
      image: profile.profileImageUrl,
      isOnboarded: profile.isOnboarded ?? false,
      role: profile.role,
    },
  };
}
