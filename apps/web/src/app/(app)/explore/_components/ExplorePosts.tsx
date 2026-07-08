'use client';

import { useGetPostRecommendationsInfinite } from '@/api/__generated__/post/post';
import { PostRecommendationType } from '@/components/feature/post/types/post';
import InfinitePostList from '@/components/feature/post/viewer/InfinitePostList';
import { toPostPreviewProps } from '@/components/feature/post/viewer/PostPreview';
import { Empty, EmptyDescription, EmptyTitle } from '@/components/ui/empty';

const FEED_PAGE_SIZE = '50';

const EMPTY_MESSAGES: Record<
  PostRecommendationType,
  { title: string; description: string }
> = {
  [PostRecommendationType.FOLLOWING]: {
    title: '팔로우한 사용자의 포스트가 없습니다',
    description: '더 많은 사용자를 팔로우하면 여기에 포스트가 모입니다.',
  },
  [PostRecommendationType.TRENDING]: {
    title: '인기 포스트가 없습니다',
    description: '잠시 후 다시 확인해주세요.',
  },
};

interface ExplorePostsProps {
  type: PostRecommendationType;
}

export default function ExplorePosts({ type }: ExplorePostsProps) {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isPending } =
    useGetPostRecommendationsInfinite(
      {
        type,
        first: FEED_PAGE_SIZE,
        after: '',
      },
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

  const posts =
    data?.pages.flatMap((page) => page.data.posts?.nodes ?? []) ?? [];

  const emptyMessage = EMPTY_MESSAGES[type];

  return (
    <InfinitePostList
      posts={posts.map((post) => toPostPreviewProps(post.content))}
      isPending={isPending}
      hasNextPage={!!hasNextPage}
      isFetchingNextPage={isFetchingNextPage}
      fetchNextPage={fetchNextPage}
      empty={
        <Empty className="min-h-80">
          <EmptyTitle>{emptyMessage.title}</EmptyTitle>
          <EmptyDescription>{emptyMessage.description}</EmptyDescription>
        </Empty>
      }
    />
  );
}
