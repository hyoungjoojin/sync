import { useQueryClient } from '@tanstack/react-query';

import {
  getCollectionPostsResponse,
  getGetCollectionQueryKey,
  useRemoveCollectionItem as useRemoveCollectionItemMutation,
} from '@/api/__generated__/collection/collection';
import type { CollectionSummary } from '@/api/__generated__/types/CollectionSummary';
import type { GetCollectionsResponse } from '@/api/__generated__/types/GetCollectionsResponse';

import { isCollectionsListKey } from '../utils';

type CollectionPostsInfiniteData = {
  pages: getCollectionPostsResponse[];
  pageParams: unknown[];
};

/**
 * 항목 ID(collectionPostId) 를 아는 모든 화면에서 쓰인다 — 상세 페이지의 항목
 * 목록과, 게시글 화면의 컬렉션 토글 다이얼로그. 상세 페이지에서는 무한 스크롤
 * 캐시에서 항목을 직접 걷어내고, 목록 캐시(memberships)가 있다면 거기서도
 * 같은 항목을 제거한다. 두 캐시 모두 postCount 를 -1 한다.
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
                        postCount: Math.max(0, collection.postCount - 1),
                      }
                    : collection,
                ),
                memberships: previous.data.memberships?.filter(
                  (membership) =>
                    membership.collectionExternalId !== externalId,
                ),
              },
            },
        );
      },
    },
  });
}
