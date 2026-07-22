import { useQueryClient } from '@tanstack/react-query';

import { useDeleteCollection as useDeleteCollectionMutation } from '@/api/__generated__/collection/collection';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';

import { isCollectionsListKey } from '../utils';

/**
 * 삭제된 컬렉션을 개인/프로젝트 컬렉션 목록 캐시에서 직접 제거한다.
 * (상세 캐시는 삭제 후 페이지를 벗어나므로 별도로 손대지 않는다.)
 */
export function useDeleteCollection() {
  const queryClient = useQueryClient();

  return useDeleteCollectionMutation({
    mutation: {
      onSuccess: (_response, { externalId }) => {
        queryClient.setQueriesData<{ data: GetCollectionsResponse }>(
          { predicate: (query) => isCollectionsListKey(query.queryKey) },
          (previous) =>
            previous && {
              ...previous,
              data: {
                ...previous.data,
                collections: previous.data.collections.filter(
                  (collection) => collection.externalId !== externalId,
                ),
              },
            },
        );
      },
    },
  });
}
