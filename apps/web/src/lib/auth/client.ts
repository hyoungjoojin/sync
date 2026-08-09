'use client';

import { useGetAuthenticatedUser } from '@/api/__generated__/profile/profile';

import { toSession } from './types';

/**
 * 미인증이면 백엔드가 401을 주고 쿼리는 error 상태로 끝나므로,
 * data는 null·isPending은 false가 된다.
 */
export function useSession() {
  const { data, isPending, refetch } = useGetAuthenticatedUser({
    query: {
      select: (response) => toSession(response.data),
    },
  });

  return { data: data ?? null, isPending, refetch };
}
