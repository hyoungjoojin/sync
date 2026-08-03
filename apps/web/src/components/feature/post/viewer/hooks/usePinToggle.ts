'use client';

import { useTranslations } from 'next-intl';
import { toast } from 'sonner';

import { usePinPost } from '@/components/feature/post/hooks/usePinPost';
import { useUnpinPost } from '@/components/feature/post/hooks/useUnpinPost';

export function usePinToggle(
  postId: number,
  projectHandle: string,
  pinned: boolean,
) {
  const t = useTranslations('pages.posts.pin');
  const { mutate: pinPost } = usePinPost();
  const { mutate: unpinPost } = useUnpinPost();

  return () => {
    const mutate = pinned ? unpinPost : pinPost;

    mutate(
      { handle: projectHandle, postId: String(postId) },
      { onError: () => toast.error(t('messages.error')) },
    );
  };
}
