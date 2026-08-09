import { useQueryClient } from '@tanstack/react-query';

import { useBookmarkPost as useBookmarkPostMutation } from '@/api/__generated__/bookmark/bookmark';
import type { ErrorType } from '@/lib/server';

import {
  type PostCacheSnapshot,
  applyBookmarkToCache,
  restorePostCache,
  snapshotPostCache,
} from './postCache';

export function useBookmarkPost() {
  const queryClient = useQueryClient();

  return useBookmarkPostMutation<ErrorType<unknown>, PostCacheSnapshot>({
    mutation: {
      onMutate: ({ postId }) => {
        const snapshot = snapshotPostCache(queryClient);
        applyBookmarkToCache(queryClient, Number(postId), true);

        return snapshot;
      },
      onError: (_error, _variables, snapshot) => {
        if (snapshot) {
          restorePostCache(queryClient, snapshot);
        }
      },
      onSuccess: (_response, { postId }) => {
        applyBookmarkToCache(queryClient, Number(postId), true);
      },
    },
  });
}
