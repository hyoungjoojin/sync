import { cache } from 'react';

import { getAuthenticatedUser } from '@/api/__generated__/profile/profile';
import SyncError, { ErrorCode } from '@/lib/error';

import type { Session } from './types';
import { toSession } from './types';

/**
 * 요청 1회당 한 번만 백엔드를 호출하도록 memoize한다.
 * 401만 미인증(null)으로 해석한다. 네트워크 오류·5xx 등 일시적인 장애까지
 * 로그아웃으로 취급하면 정상 세션이 /about 으로 튕기므로 그대로 전파한다.
 */
export const getSession = cache(async (): Promise<Session | null> => {
  try {
    const { data } = await getAuthenticatedUser();

    return toSession(data);
  } catch (error) {
    if (error instanceof SyncError && error.code === ErrorCode.UNAUTHORIZED) {
      return null;
    }

    throw error;
  }
});
