'use client';

import { useTranslations } from 'next-intl';

import { useGetUserPostsInfinite } from '@/api/__generated__/post/post';
import { useGetProfileByHandle } from '@/api/__generated__/profile/profile';
import { PostType } from '@/components/feature/post/types/post';
import InfinitePostList from '@/components/feature/post/viewer/InfinitePostList';
import PostListMessage from '@/components/feature/post/viewer/PostListMessage';
import { toPostPreviewProps } from '@/components/feature/post/viewer/PostPreview';

const QUESTIONS_PAGE_SIZE = '10';

interface ProfileQuestionsProps {
  handle: string;
}

export default function ProfileQuestions({ handle }: ProfileQuestionsProps) {
  const t = useTranslations('pages.profile.tabs.questions');

  const {
    data: profile,
    isPending: isProfilePending,
    isError: isProfileError,
  } = useGetProfileByHandle(handle);

  const userId = profile?.data.userId;

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isPending: isQuestionsPending,
    isError: isQuestionsError,
  } = useGetUserPostsInfinite(
    userId ?? '',
    {
      first: QUESTIONS_PAGE_SIZE,
      after: '',
      type: PostType.QUESTION,
    },
    {
      query: {
        enabled: !!userId,
        getNextPageParam: (lastPage) => {
          const pageInfo = lastPage.data.posts?.pageInfo;
          return pageInfo?.hasNextPage
            ? (pageInfo.endCursor ?? undefined)
            : undefined;
        },
      },
    },
  );

  const posts =
    data?.pages.flatMap((page) => page.data.posts?.nodes ?? []) ?? [];

  const isPending = isProfilePending || (!!userId && isQuestionsPending);
  const isError = isProfileError || isQuestionsError;

  return (
    <InfinitePostList
      posts={posts.map((post) => toPostPreviewProps(post.content))}
      isPending={isPending}
      isError={isError}
      hasNextPage={!!hasNextPage}
      isFetchingNextPage={isFetchingNextPage}
      fetchNextPage={fetchNextPage}
      empty={<PostListMessage message={t('empty')} />}
      error={<PostListMessage message={t('error')} variant="destructive" />}
      end={
        <div className="py-4 text-center">
          <p className="text-xs text-muted-foreground">{t('end')}</p>
        </div>
      }
    />
  );
}
