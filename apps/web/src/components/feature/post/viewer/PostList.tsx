'use client';

import { useIntersectionObserver } from '@uidotdev/usehooks';
import { type ReactNode, useEffect } from 'react';

import { Skeleton } from '@/components/ui/skeleton';
import { Spinner } from '@/components/ui/spinner';

import { PostPreviewCard } from './PostCard';
import type { PostViewSource } from './types';

interface PostListProps {
  items: PostViewSource[];
  isPending: boolean;
  isError?: boolean;
  hasNextPage: boolean;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
  empty: ReactNode;
  error?: ReactNode;
  end?: ReactNode;
  skeletonCount?: number;
}

export default function PostList({
  items,
  isPending,
  isError,
  hasNextPage,
  isFetchingNextPage,
  fetchNextPage,
  empty,
  error,
  end,
  skeletonCount = 3,
}: PostListProps) {
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
        {Array.from({ length: skeletonCount }).map((_, index) => (
          <Skeleton key={index} className="h-40 w-full" />
        ))}
      </div>
    );
  }

  if (isError) {
    return error;
  }

  if (items.length === 0) {
    return empty;
  }

  return (
    <div className="space-y-4">
      {items.map((item) => (
        <PostPreviewCard key={item.summary.id} source={item} />
      ))}

      <div ref={ref} className="py-4">
        {isFetchingNextPage && (
          <div className="flex justify-center">
            <Spinner />
          </div>
        )}
      </div>

      {!hasNextPage && end}
    </div>
  );
}
