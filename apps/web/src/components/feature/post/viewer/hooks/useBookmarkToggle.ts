'use client';

import { useBookmarkPost } from '@/components/feature/post/hooks/useBookmarkPost';
import { useUnbookmarkPost } from '@/components/feature/post/hooks/useUnbookmarkPost';

export function useBookmarkToggle(postId: number, bookmarked: boolean) {
  const { mutate: bookmarkPost } = useBookmarkPost();
  const { mutate: unbookmarkPost } = useUnbookmarkPost();

  return () => {
    const mutate = bookmarked ? unbookmarkPost : bookmarkPost;

    mutate({ postId: String(postId) });
  };
}
