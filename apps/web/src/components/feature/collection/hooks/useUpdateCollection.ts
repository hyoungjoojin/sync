import { useQueryClient } from '@tanstack/react-query';

import {
  getGetCollectionQueryKey,
  useUpdateCollection as useUpdateCollectionMutation,
} from '@/api/__generated__/collection/collection';
import type { CollectionSummary } from '@/api/__generated__/types/CollectionSummary';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';
import type { UpdateCollectionRequest } from '@/api/__generated__/types/UpdateCollectionRequest';

import { isCollectionsListKey } from '../utils';

/**
 * 변경 요청에서 실제로 전달된 필드만 골라 상세/목록 캐시에 직접 반영한다.
 * (생략된 필드는 서버에서 기존 값을 유지하므로 캐시에서도 건드리지 않는다.)
 */
function pickUpdates(request: UpdateCollectionRequest) {
  const updates: Partial<
    Pick<CollectionSummary, 'name' | 'description' | 'isPublic'>
  > = {};
  if (request.name != null) {
    updates.name = request.name;
  }
  if (request.description !== undefined) {
    updates.description = request.description;
  }
  if (request.isPublic != null) {
    updates.isPublic = request.isPublic;
  }
  return updates;
}

export function useUpdateCollection() {
  const queryClient = useQueryClient();

  return useUpdateCollectionMutation({
    mutation: {
      onSuccess: (_response, { externalId, data }) => {
        const updates = pickUpdates(data ?? {});

        queryClient.setQueryData<{ data: CollectionSummary }>(
          getGetCollectionQueryKey(externalId),
          (previous) =>
            previous && {
              ...previous,
              data: { ...previous.data, ...updates },
            },
        );

        queryClient.setQueriesData<{ data: GetCollectionsResponse }>(
          { predicate: (query) => isCollectionsListKey(query.queryKey) },
          (previous) =>
            previous && {
              ...previous,
              data: {
                ...previous.data,
                collections: previous.data.collections.map((collection) =>
                  collection.externalId === externalId
                    ? { ...collection, ...updates }
                    : collection,
                ),
              },
            },
        );
      },
    },
  });
}
