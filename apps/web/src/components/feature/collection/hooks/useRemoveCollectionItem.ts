import { useQueryClient } from '@tanstack/react-query';

import {
  getCollectionPostsResponse,
  getGetCollectionQueryKey,
  useRemoveCollectionItem as useRemoveCollectionItemMutation,
} from '@/api/__generated__/collection/collection';
import type { CollectionSummary } from '@/api/__generated__/types/CollectionSummary';

type CollectionPostsInfiniteData = {
  pages: getCollectionPostsResponse[];
  pageParams: unknown[];
};

/**
 * 상세 페이지에서는 게시글 목록이 이미 캐시에 있으므로, 제거된 항목을
 * collectionPostId 로 찾아 무한 스크롤 캐시에서 직접 삭제하고 postCount 를
 * 직접 -1 한다. 목록을 다시 불러오지 않는다.
 */
export function useRemoveCollectionItem() {
  const queryClient = useQueryClient();

  return useRemoveCollectionItemMutation({
    mutation: {
      onSuccess: (_response, { externalId, collectionPostId }) => {
        const postsKeyPrefix = `/collections/${externalId}/posts`;

        queryClient.setQueriesData<CollectionPostsInfiniteData>(
          {
            predicate: (query) => query.queryKey[1] === postsKeyPrefix,
          },
          (previous) =>
            previous && {
              ...previous,
              pages: previous.pages.map((page) => ({
                ...page,
                data: {
                  ...page.data,
                  posts: {
                    ...page.data.posts,
                    nodes: page.data.posts.nodes.filter(
                      (node) =>
                        String(node.content.collectionPostId) !==
                        collectionPostId,
                    ),
                  },
                },
              })),
            },
        );

        queryClient.setQueryData<{ data: CollectionSummary }>(
          getGetCollectionQueryKey(externalId),
          (previous) =>
            previous && {
              ...previous,
              data: {
                ...previous.data,
                postCount: Math.max(0, previous.data.postCount - 1),
              },
            },
        );
      },
    },
  });
}
