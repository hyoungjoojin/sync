'use client';

import { ProhibitIcon } from '@phosphor-icons/react';
import { useIntersectionObserver } from '@uidotdev/usehooks';
import { useTranslations } from 'next-intl';
import { useEffect } from 'react';

import { PostPreviewCard } from '@/components/feature/post/viewer/PostCard';
import { Card } from '@/components/ui/card';
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty';
import { Skeleton } from '@/components/ui/skeleton';
import { Spinner } from '@/components/ui/spinner';

import type { CollectionItem } from './utils';

interface CollectionItemListProps {
  items: CollectionItem[];
  isPending: boolean;
  hasNextPage: boolean;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
}

export function CollectionItemList({
  items,
  isPending,
  hasNextPage,
  isFetchingNextPage,
  fetchNextPage,
}: CollectionItemListProps) {
  const t = useTranslations('pages.collections');

  const [ref, entry] = useIntersectionObserver({
    threshold: 0.2,
    root: null,
    rootMargin: '400px',
  });

  useEffect(() => {
    if (entry?.isIntersecting && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [entry?.isIntersecting, hasNextPage, isFetchingNextPage, fetchNextPage]);

  if (isPending) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 3 }).map((_, index) => (
          <Skeleton key={index} className="h-40 w-full" />
        ))}
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <Empty className="min-h-80">
        <EmptyHeader>
          <EmptyTitle>{t('empty.title')}</EmptyTitle>
          <EmptyDescription>{t('empty.description')}</EmptyDescription>
        </EmptyHeader>
      </Empty>
    );
  }

  return (
    <div className="space-y-4">
      {items.map((item) =>
        item.summary ? (
          <PostPreviewCard key={item.collectionPostId} summary={item.summary} />
        ) : (
          <Card
            key={item.collectionPostId}
            className="text-muted-foreground flex flex-row items-center gap-3 p-4 text-sm"
          >
            <ProhibitIcon className="size-5 shrink-0" />
            <span>{t('item.unavailable')}</span>
          </Card>
        ),
      )}

      <div ref={ref} className="py-4">
        {isFetchingNextPage && (
          <div className="flex justify-center">
            <Spinner />
          </div>
        )}
      </div>
    </div>
  );
}
