'use client';

import { WarningCircleIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { useGetPostRecommendationsInfinite } from '@/api/__generated__/post/post';
import { PostRecommendationType } from '@/components/feature/post/types/post';
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

import { createHomeFeedParams } from './homeFeed';

export default function HomeFeed() {
  const t = useTranslations('pages.home.feed');
  const tEmpty = useTranslations('pages.home.feed.empty');
  const [type, setType] = useState<PostRecommendationType>(
    PostRecommendationType.TRENDING,
  );

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
  } = useGetPostRecommendationsInfinite(createHomeFeedParams(type, '50'), {
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

  const empty =
    type === PostRecommendationType.FOLLOWING ? (
      <Empty className="min-h-80">
        <EmptyHeader>
          <EmptyTitle>{tEmpty('following.title')}</EmptyTitle>
          <EmptyDescription>{tEmpty('following.description')}</EmptyDescription>
        </EmptyHeader>
        <EmptyContent>
          <LinkButton href={ROUTES.EXPLORE_TRENDING()} size="sm">
            {tEmpty('following.explore')}
          </LinkButton>
        </EmptyContent>
      </Empty>
    ) : (
      <Empty className="min-h-80">
        <EmptyHeader>
          <EmptyTitle>{tEmpty('trending.title')}</EmptyTitle>
          <EmptyDescription>{tEmpty('trending.description')}</EmptyDescription>
        </EmptyHeader>
        <EmptyContent>
          <LinkButton href={ROUTES.NEW_POST()} size="sm">
            {tEmpty('trending.write')}
          </LinkButton>
        </EmptyContent>
      </Empty>
    );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Tabs
          value={type}
          onValueChange={(value) => setType(value as PostRecommendationType)}
        >
          <TabsList>
            <TabsTrigger value={PostRecommendationType.TRENDING}>
              {t('tabs.trending')}
            </TabsTrigger>
            <TabsTrigger value={PostRecommendationType.FOLLOWING}>
              {t('tabs.following')}
            </TabsTrigger>
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
        empty={empty}
      />
    </div>
  );
}
