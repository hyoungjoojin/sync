'use client';

import { BookmarkSimpleIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import { Button } from '@/components/ui/button';
import { useRequireAuth } from '@/hooks/use-require-auth';
import { cn } from '@/lib/utils';

import { useBookmarkToggle } from '../hooks/useBookmarkToggle';
import type { PostSummary } from '../types';

export function PostBookmarkButton({
  summary,
  onClick,
}: {
  summary: PostSummary;
  onClick?: (event: React.MouseEvent) => void;
}) {
  const t = useTranslations('pages.posts.bookmark');
  const { requireAuth } = useRequireAuth();

  const { bookmarked } = summary;
  const toggleBookmark = useBookmarkToggle(summary.id, bookmarked);

  return (
    <Button
      variant="ghost"
      size="icon-sm"
      aria-label={bookmarked ? t('remove') : t('add')}
      onClick={(event) => {
        onClick?.(event);

        if (!requireAuth({ intent: 'bookmark' })) {
          return;
        }

        toggleBookmark();
      }}
    >
      <BookmarkSimpleIcon
        className={cn(bookmarked && 'fill-primary text-primary')}
        weight={bookmarked ? 'fill' : 'regular'}
      />
    </Button>
  );
}
