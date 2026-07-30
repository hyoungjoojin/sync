'use client';

import { useTranslations } from 'next-intl';
import { toast } from 'sonner';

export function useCopyPostLink(postPath: string) {
  const t = useTranslations('pages.posts.copy-link');

  return async () => {
    try {
      await navigator.clipboard.writeText(window.location.origin + postPath);
      toast.success(t('messages.success'));
    } catch {
      toast.error(t('messages.error'));
    }
  };
}
