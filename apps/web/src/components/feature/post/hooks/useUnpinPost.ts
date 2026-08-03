import { useQueryClient } from '@tanstack/react-query';

import { useUnpinPost as useUnpinPostMutation } from '@/api/__generated__/post/post';
import type { ErrorType } from '@/lib/server';

import {
  type PostCacheSnapshot,
  applyPinToCache,
  restorePostCache,
  snapshotPostCache,
} from './postCache';

export function useUnpinPost() {
  const queryClient = useQueryClient();

  return useUnpinPostMutation<ErrorType<unknown>, PostCacheSnapshot>({
    mutation: {
      onMutate: ({ postId, handle }) => {
        const snapshot = snapshotPostCache(queryClient);
        applyPinToCache(queryClient, Number(postId), handle, false);

        return snapshot;
      },
      onError: (_error, _variables, snapshot) => {
        if (snapshot) {
          restorePostCache(queryClient, snapshot);
        }
      },
      onSuccess: (_response, { postId, handle }) => {
        applyPinToCache(queryClient, Number(postId), handle, false);
      },
    },
  });
}
