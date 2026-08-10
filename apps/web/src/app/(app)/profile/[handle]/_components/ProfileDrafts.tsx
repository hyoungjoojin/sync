'use client';

import { PlusIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import { useGetDraftPostsInfinite } from '@/api/__generated__/post/post';
import { PostScope } from '@/components/feature/post/types/post';
import PostList from '@/components/feature/post/viewer/PostList';
import PostListMessage from '@/components/feature/post/viewer/error/PostListMessage';
import { toPostSummary } from '@/components/feature/post/viewer/types';
import { LinkButton } from '@/components/ui/button';
import ROUTES from '@/util/routes';

const PAGE_SIZE = '50';

export default function ProfileDrafts() {
  const t = useTranslations('pages.posts.drafts');

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isError,
    isFetchingNextPage,
    isPending,
  } = useGetDraftPostsInfinite(
    { first: PAGE_SIZE, scope: PostScope.PUBLIC },
    {
      query: {
        getNextPageParam: (lastPage) => {
          const pageInfo = lastPage.data.posts?.pageInfo;
          return pageInfo?.hasNextPage
            ? (pageInfo.endCursor ?? undefined)
            : undefined;
        },
      },
    },
  );
  const drafts =
    data?.pages.flatMap((page) => page.data.posts?.nodes ?? []) ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold">{t('title')}</h1>
          <p className="text-muted-foreground text-sm">{t('description')}</p>
        </div>

        <LinkButton href={ROUTES.NEW_POST()}>
          <PlusIcon />
          {t('new-post')}
        </LinkButton>
      </div>

      <PostList
        items={drafts.map((draft) => toPostSummary(draft.content))}
        isPending={isPending}
        isError={isError}
        hasNextPage={!!hasNextPage}
        isFetchingNextPage={isFetchingNextPage}
        fetchNextPage={fetchNextPage}
        empty={
          <div className="flex flex-col items-center gap-3 py-16 text-center">
            <p className="font-medium">{t('empty.title')}</p>
            <p className="text-sm text-muted-foreground">
              {t('empty.description')}
            </p>
            <LinkButton href={ROUTES.NEW_POST()}>
              {t('empty.action')}
            </LinkButton>
          </div>
        }
        error={
          <PostListMessage
            message={t('error.description')}
            variant="destructive"
          />
        }
      />
    </div>
  );
}
