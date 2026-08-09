import { useQueryClient } from '@tanstack/react-query';

import {
  getGetProjectCollectionsQueryKey,
  useCreateProjectCollection as useCreateProjectCollectionMutation,
} from '@/api/__generated__/collection/collection';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';
import type { GetCollectionsResponseCollectionsItem } from '@/api/__generated__/types/GetCollectionsResponseCollectionsItem';

/**
 * 프로젝트 컬렉션 생성 후, 목록을 다시 불러오지 않고 프로젝트 컬렉션 캐시
 * 맨 앞에 새 컬렉션을 직접 삽입한다. 서버 응답은 externalId 만 주므로 나머지
 * 필드는 요청 값으로 낙관적으로 구성한다. (creatorId 는 목록 카드에서 쓰지
 * 않으므로 0 으로 둔다.)
 */
export function useCreateProjectCollection() {
  const queryClient = useQueryClient();

  return useCreateProjectCollectionMutation({
    mutation: {
      onSuccess: (response, { handle, data }) => {
        if (!data) {
          return;
        }

        const created: GetCollectionsResponseCollectionsItem = {
          externalId: response.data.externalId,
          scope: 'WORKSPACE',
          creatorId: 0,
          name: data.name,
          description: data.description ?? null,
          isPublic: data.isPublic ?? true,
          postCount: 0,
          projectHandle: handle,
        };

        queryClient.setQueryData<{ data: GetCollectionsResponse }>(
          getGetProjectCollectionsQueryKey(handle),
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
