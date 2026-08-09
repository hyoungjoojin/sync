import { useQueryClient } from '@tanstack/react-query';

import { useUnlikePost as useUnlikePostMutation } from '@/api/__generated__/post/post';
import type { ErrorType } from '@/lib/server';

import {
  type PostCacheSnapshot,
  applyLikeToCache,
  restorePostCache,
  snapshotPostCache,
} from './postCache';

export function useUnlikePost() {
  const queryClient = useQueryClient();

  return useUnlikePostMutation<ErrorType<unknown>, PostCacheSnapshot>({
    mutation: {
      onMutate: ({ postId }) => {
        const snapshot = snapshotPostCache(queryClient);
        applyLikeToCache(queryClient, Number(postId), false);

        return snapshot;
      },
      onError: (_error, _variables, snapshot) => {
        if (snapshot) {
          restorePostCache(queryClient, snapshot);
        }
      },
      onSuccess: (_response, { postId }) => {
        applyLikeToCache(queryClient, Number(postId), false);
      },
    },
  });
}
