import { useQueryClient } from '@tanstack/react-query';

import {
  getGetPostCommentsInfiniteQueryKey,
  useAcceptComment as useAcceptCommentMutation,
  useUnacceptComment as useUnacceptCommentMutation,
} from '@/api/__generated__/comment/comment';
import { getGetPostBySlugQueryKey } from '@/api/__generated__/post/post';

import { COMMENT_PAGE_SIZE } from '../constants';

export function useCommentAcceptance(slug: string) {
  const queryClient = useQueryClient();

  // 채택 여부는 게시글의 해결 상태(resolved)까지 바꾸므로 게시글 조회도 함께 무효화한다.
  const invalidateComments = async () => {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: getGetPostCommentsInfiniteQueryKey(slug, {
          first: COMMENT_PAGE_SIZE,
        }),
      }),
      queryClient.invalidateQueries({
        queryKey: getGetPostBySlugQueryKey(slug),
      }),
    ]);
  };

  const { mutate: acceptComment, isPending: isAccepting } =
    useAcceptCommentMutation({
      mutation: { onSuccess: invalidateComments },
    });

  const { mutate: unacceptComment, isPending: isUnaccepting } =
    useUnacceptCommentMutation({
      mutation: { onSuccess: invalidateComments },
    });

  return {
    acceptComment: (commentId: string) => acceptComment({ commentId }),
    unacceptComment: (commentId: string) => unacceptComment({ commentId }),
    isPending: isAccepting || isUnaccepting,
  };
}
