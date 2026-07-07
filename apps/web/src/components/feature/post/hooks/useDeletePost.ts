import { useQueryClient } from '@tanstack/react-query';

import { useDeletePost as useDeletePostMutation } from '@/api/__generated__/post/post';

function isPostsQueryKey(queryKey: readonly unknown[]) {
  return typeof queryKey[1] === 'string' && queryKey[1].startsWith('/posts');
}

export function useDeletePost() {
  const queryClient = useQueryClient();

  return useDeletePostMutation({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          predicate: (query) => isPostsQueryKey(query.queryKey),
        });
      },
    },
  });
}
