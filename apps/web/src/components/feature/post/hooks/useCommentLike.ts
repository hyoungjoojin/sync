import { useQueryClient } from '@tanstack/react-query';

import {
  getGetPostCommentsInfiniteQueryKey,
  type getPostCommentsResponse,
  useLikeComment as useLikeCommentMutation,
  useUnlikeComment as useUnlikeCommentMutation,
} from '@/api/__generated__/comment/comment';
import type { ErrorType } from '@/lib/server';

import { COMMENT_PAGE_SIZE } from '../constants';

interface CommentsCache {
  pages: getPostCommentsResponse[];
  pageParams: unknown[];
}

export function useCommentLike(slug: string) {
  const queryClient = useQueryClient();

  const queryKey = getGetPostCommentsInfiniteQueryKey(slug, {
    first: COMMENT_PAGE_SIZE,
  });

  // 낙관적 갱신(onMutate)과 성공 확정(onSuccess)이 같은 패치를 두 번 적용하므로,
  // 이미 반영된 댓글은 건드리지 않아야 좋아요 수가 두 번 오르지 않는다.
  const applyLikeToCache = (commentId: number, liked: boolean) => {
    queryClient.setQueryData<CommentsCache>(queryKey, (previous) => {
      if (!previous) {
        return previous;
      }

      return {
        ...previous,
        pages: previous.pages.map((page) => {
          const comments = page.data.comments;
          if (!comments) {
            return page;
          }

          let changed = false;
          const nodes = comments.nodes.map((node) => {
            if (node.content.id !== commentId || node.content.liked === liked) {
              return node;
            }

            changed = true;

            return {
              ...node,
              content: {
                ...node.content,
                liked,
                likeCount: Math.max(
                  node.content.likeCount + (liked ? 1 : -1),
                  0,
                ),
              },
            };
          });

          if (!changed) {
            return page;
          }

          return {
            ...page,
            data: { ...page.data, comments: { ...comments, nodes } },
          };
        }),
      };
    });
  };

  const mutationOptions = (liked: boolean) => ({
    mutation: {
      onMutate: ({ commentId }: { commentId: string }) => {
        const snapshot = queryClient.getQueryData<CommentsCache>(queryKey);
        applyLikeToCache(Number(commentId), liked);

        return snapshot;
      },
      onError: (
        _error: ErrorType<unknown>,
        _variables: { commentId: string },
        snapshot: CommentsCache | undefined,
      ) => {
        if (snapshot) {
          queryClient.setQueryData<CommentsCache>(queryKey, snapshot);
        }
      },
      onSuccess: (_response: unknown, { commentId }: { commentId: string }) => {
        applyLikeToCache(Number(commentId), liked);
      },
    },
  });

  const { mutate: likeComment } = useLikeCommentMutation<
    ErrorType<unknown>,
    CommentsCache | undefined
  >(mutationOptions(true));
  const { mutate: unlikeComment } = useUnlikeCommentMutation<
    ErrorType<unknown>,
    CommentsCache | undefined
  >(mutationOptions(false));

  return {
    toggleLike: (commentId: number, liked: boolean) =>
      liked
        ? unlikeComment({ commentId: String(commentId) })
        : likeComment({ commentId: String(commentId) }),
  };
}
