import { useQueryClient } from '@tanstack/react-query';

import {
  getGetCollectionQueryKey,
  useAddPostToCollection as useAddPostToCollectionMutation,
} from '@/api/__generated__/collection/collection';
import type { CollectionSummary } from '@/api/__generated__/types/CollectionSummary';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';

import { isCollectionsListKey } from '../utils';

/**
 * 게시글 추가 응답은 본문이 없으므로(204) 새 항목을 목록에 직접 삽입할 수는 없다.
 * 대신 상세/목록 캐시의 postCount 를 +1 하고, containsPost(추가 여부 표시)를
 * true 로 바꾼다. 컬렉션은 externalId 로 유일하므로, 개인/프로젝트 목록의 모든
 * 파라미터 변형을 프레디킷으로 훑어 매칭되는 항목만 갱신한다.
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
                    ? {
                        ...collection,
                        postCount: collection.postCount + 1,
                        containsPost: true,
                      }
                    : collection,
                ),
              },
            },
        );
      },
    },
  });
}
