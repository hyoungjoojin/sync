import { useQueryClient } from '@tanstack/react-query';

import { useUnbookmarkPost as useUnbookmarkPostMutation } from '@/api/__generated__/bookmark/bookmark';
import type { ErrorType } from '@/lib/server';

import {
  type PostCacheSnapshot,
  applyBookmarkToCache,
  restorePostCache,
  snapshotPostCache,
} from './postCache';

export function useUnbookmarkPost() {
  const queryClient = useQueryClient();

  return useUnbookmarkPostMutation<ErrorType<unknown>, PostCacheSnapshot>({
    mutation: {
      onMutate: ({ postId }) => {
        const snapshot = snapshotPostCache(queryClient);
        applyBookmarkToCache(queryClient, Number(postId), false);

        return snapshot;
      },
      onError: (_error, _variables, snapshot) => {
        if (snapshot) {
          restorePostCache(queryClient, snapshot);
        }
      },
      onSuccess: (_response, { postId }) => {
        applyBookmarkToCache(queryClient, Number(postId), false);
      },
    },
  });
}
