import { useQueryClient } from '@tanstack/react-query';

import {
  getGetUserCollectionsQueryKey,
  useCreateCollection as useCreateCollectionMutation,
} from '@/api/__generated__/collection/collection';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';
import type { GetCollectionsResponseCollectionsItem } from '@/api/__generated__/types/GetCollectionsResponseCollectionsItem';
import { useSession } from '@/lib/auth/client';

/**
 * 컬렉션 생성 후, 목록을 다시 불러오지 않고 사용자 컬렉션 캐시 맨 앞에
 * 새 컬렉션을 직접 삽입한다. 서버 응답은 externalId 만 주므로 나머지 필드는
 * 요청 값과 현재 세션으로 낙관적으로 구성한다.
 */
export function useCreateCollection() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useCreateCollectionMutation({
    mutation: {
      onSuccess: (response, { data }) => {
        const userId = session?.user.id;
        if (!userId || !data) {
          return;
        }

        const created: GetCollectionsResponseCollectionsItem = {
          externalId: response.data.externalId,
          scope: 'PERSONAL',
          creatorId: Number(userId),
          name: data.name,
          description: data.description ?? null,
          isPublic: data.isPublic ?? true,
          postCount: 0,
          projectHandle: null,
        };

        queryClient.setQueryData<{ data: GetCollectionsResponse }>(
          getGetUserCollectionsQueryKey(String(userId)),
          (previous) =>
            previous && {
              ...previous,
              data: {
                ...previous.data,
                collections: [created, ...previous.data.collections],
              },
            },
        );
      },
    },
  });
}
