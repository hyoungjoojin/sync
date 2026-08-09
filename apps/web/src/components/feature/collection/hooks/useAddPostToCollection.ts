import { useQueryClient } from '@tanstack/react-query';

import {
  getGetCollectionQueryKey,
  useAddPostToCollection as useAddPostToCollectionMutation,
} from '@/api/__generated__/collection/collection';
import type { CollectionSummary } from '@/api/__generated__/types/CollectionSummary';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';

import { isCollectionsListKey } from '../utils';

/**
 * 게시글 추가 응답은 본문이 없으므로(204) 새로 생긴 collectionPostId 를 알 수
 * 없다. 상세/목록 캐시의 postCount 는 +1 로 직접 패치하지만, 목록 캐시의
 * memberships(제거에 필요한 항목 ID)는 직접 채워 넣지 않고 무효화해 다음
 * 조회 때 실제 항목 ID 를 받아온다.
 */
export function useAddPostToCollection() {
  const queryClient = useQueryClient();

  return useAddPostToCollectionMutation({
    mutation: {
      onSuccess: (_response, { externalId }) => {
        queryClient.setQueryData<{ data: CollectionSummary }>(
          getGetCollectionQueryKey(externalId),
          (previous) =>
            previous && {
              ...previous,
              data: {
                ...previous.data,
                postCount: previous.data.postCount + 1,
              },
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
                    ? { ...collection, postCount: collection.postCount + 1 }
                    : collection,
                ),
              },
            },
        );

        queryClient.invalidateQueries({
          predicate: (query) => isCollectionsListKey(query.queryKey),
        });
      },
    },
  });
}
