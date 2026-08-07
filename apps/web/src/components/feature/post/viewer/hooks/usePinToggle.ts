'use client';

import { useTranslations } from 'next-intl';
import { toast } from 'sonner';

import { usePinPost } from '@/components/feature/post/hooks/usePinPost';
import { useUnpinPost } from '@/components/feature/post/hooks/useUnpinPost';
import SyncError, { ErrorCode } from '@/lib/error';

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
      {
        onError: (error) => {
          if (error instanceof SyncError) {
            switch (error.code) {
              case ErrorCode.POST_PIN_LIMIT_EXCEEDED:
                toast.error(t('messages.limit-exceeded'));
                return;
              case ErrorCode.POST_NOT_FOUND:
              case ErrorCode.PROJECT_NOT_FOUND:
                toast.error(t('messages.not-found'));
                return;
            }
          }

          toast.error(t('messages.error'));
        },
      },
    );
  };
}
