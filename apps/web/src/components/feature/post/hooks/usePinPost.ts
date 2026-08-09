import { useQueryClient } from '@tanstack/react-query';

import { usePinPost as usePinPostMutation } from '@/api/__generated__/post/post';
import type { ErrorType } from '@/lib/server';

import {
  type PostCacheSnapshot,
  applyPinToCache,
  restorePostCache,
  snapshotPostCache,
} from './postCache';

export function usePinPost() {
  const queryClient = useQueryClient();

  return usePinPostMutation<ErrorType<unknown>, PostCacheSnapshot>({
    mutation: {
      onMutate: ({ postId, handle }) => {
        const snapshot = snapshotPostCache(queryClient);
        applyPinToCache(queryClient, Number(postId), handle, true);

        return snapshot;
      },
      onError: (_error, _variables, snapshot) => {
        if (snapshot) {
          restorePostCache(queryClient, snapshot);
        }
      },
      onSuccess: (_response, { postId, handle }) => {
        applyPinToCache(queryClient, Number(postId), handle, true);
      },
    },
  });
}
