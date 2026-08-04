import type { MutateOptions } from '@tanstack/react-query';
import { useQueryClient } from '@tanstack/react-query';

import {
  type deleteComment as deleteCommentApi,
  getGetPostCommentsInfiniteQueryKey,
  useDeleteComment as useDeleteCommentMutation,
} from '@/api/__generated__/comment/comment';
import type { ErrorType } from '@/lib/server';

import { COMMENT_PAGE_SIZE } from '../constants';
import { applyCommentCountToCache } from './postCache';

export function useCommentDelete(slug: string, postId: number) {
  const queryClient = useQueryClient();

  const { mutate, isPending } = useDeleteCommentMutation({
    mutation: {
      onSuccess: async () => {
        applyCommentCountToCache(queryClient, postId, -1);

        await queryClient.invalidateQueries({
          queryKey: getGetPostCommentsInfiniteQueryKey(slug, {
            first: COMMENT_PAGE_SIZE,
          }),
        });
      },
    },
  });

  return {
    deleteComment: (
      commentId: number,
      options?: MutateOptions<
        Awaited<ReturnType<typeof deleteCommentApi>>,
        ErrorType<unknown>,
        { commentId: string }
      >,
    ) => mutate({ commentId: String(commentId) }, options),
    isPending,
  };
}
