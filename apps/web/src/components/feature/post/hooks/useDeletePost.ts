import { useQueryClient } from '@tanstack/react-query';

import { useDeletePost as useDeletePostMutation } from '@/api/__generated__/post/post';

function isPostsQueryKey(queryKey: readonly unknown[]) {
  const key = queryKey[0] === 'infinite' ? queryKey[1] : queryKey[0];
  return typeof key === 'string' && key.startsWith('/posts');
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
