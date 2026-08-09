'use client';

import { WarningCircleIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { useGetPostRecommendationsInfinite } from '@/api/__generated__/post/post';
import PostList from '@/components/feature/post/viewer/PostList';
import { toPostSummary } from '@/components/feature/post/viewer/types';
import { Button, LinkButton } from '@/components/ui/button';
import {
  Empty,
  EmptyContent,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from '@/components/ui/empty';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import ROUTES from '@/util/routes';

import { type HomeFeedFilter, createHomeFeedParams } from './homeFeed';

export default function HomeFeed() {
  const t = useTranslations('pages.home.feed');
  const tEmpty = useTranslations('pages.home.feed.empty');
  const [filter, setFilter] = useState<HomeFeedFilter>('all');

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isError,
    isFetching,
    isFetchingNextPage,
    isFetchNextPageError,
    isPending,
    refetch,
  } = useGetPostRecommendationsInfinite(createHomeFeedParams(filter, '50'), {
    query: {
      getNextPageParam: (lastPage) => {
        const pageInfo = lastPage.data.posts?.pageInfo;
        return pageInfo?.hasNextPage
          ? (pageInfo.endCursor ?? undefined)
          : undefined;
      },
    },
  });

  const posts =
    data?.pages.flatMap((page) => page.data.posts?.nodes ?? []) ?? [];
  const hasInitialError = isError && !data;

  const errorState = (
    <Empty className="min-h-80">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <WarningCircleIcon />
        </EmptyMedia>
        <EmptyTitle>{t('error.title')}</EmptyTitle>
        <EmptyDescription>{t('error.description')}</EmptyDescription>
      </EmptyHeader>
      <EmptyContent>
        <Button
          size="sm"
          variant="outline"
          disabled={isFetching}
          onClick={() => void refetch()}
        >
          {t('error.retry')}
        </Button>
      </EmptyContent>
    </Empty>
  );

  const nextPageError = (
    <div className="flex flex-col items-center gap-2 text-center">
      <p className="text-muted-foreground text-sm">
        {t('pagination-error.description')}
      </p>
      <Button
        size="sm"
        variant="outline"
        disabled={isFetchingNextPage}
        onClick={() => void fetchNextPage()}
      >
        {t('pagination-error.retry')}
      </Button>
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Tabs
          value={filter}
          onValueChange={(value) => setFilter(value as HomeFeedFilter)}
        >
          <TabsList>
            <TabsTrigger value="all">{t('tabs.all')}</TabsTrigger>
            <TabsTrigger value="shorts">{t('tabs.shorts')}</TabsTrigger>
            <TabsTrigger value="articles">{t('tabs.articles')}</TabsTrigger>
            <TabsTrigger value="questions">{t('tabs.questions')}</TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      <PostList
        items={posts.map((post) => toPostSummary(post.content))}
        isPending={isPending}
        isError={hasInitialError}
        hasNextPage={!!hasNextPage}
        isFetchingNextPage={isFetchingNextPage}
        isFetchNextPageError={isFetchNextPageError}
        fetchNextPage={fetchNextPage}
        error={errorState}
        nextPageError={nextPageError}
        empty={
          <Empty className="min-h-80">
            <EmptyHeader>
              <EmptyTitle>{tEmpty('title')}</EmptyTitle>
              <EmptyDescription>{tEmpty('description')}</EmptyDescription>
            </EmptyHeader>
            <EmptyContent>
              <LinkButton href={ROUTES.EXPLORE_TRENDING()} size="sm">
                {tEmpty('explore')}
              </LinkButton>
            </EmptyContent>
          </Empty>
        }
      />
    </div>
  );
}
