'use client';

import { useGetCommentedPostsInfinite } from '@/api/__generated__/post/post';
import PostList from '@/components/feature/post/viewer/PostList';
import PostListMessage from '@/components/feature/post/viewer/error/PostListMessage';
import { toPostViewSource } from '@/components/feature/post/viewer/types';
import { useSession } from '@/lib/auth/client';

const PAGE_SIZE = '10';

interface ProjectMyCommentsProps {
  handle: string;
}

export default function ProjectMyComments({ handle }: ProjectMyCommentsProps) {
  const { data: session } = useSession();
  const userId = session?.user.id;

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isPending,
    isError,
  } = useGetCommentedPostsInfinite(
    userId ?? '',
    { first: PAGE_SIZE, projectHandle: handle },
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

  return (
    <section className="space-y-3">
      <h1 className="text-xl font-semibold">내가 댓글단 글</h1>

      <PostList
        items={posts.map((post) => toPostViewSource(post.content))}
        isPending={isPending || !userId}
        isError={isError}
        hasNextPage={!!hasNextPage}
        isFetchingNextPage={isFetchingNextPage}
        fetchNextPage={fetchNextPage}
        empty={<PostListMessage message="아직 댓글을 남긴 게시물이 없어요." />}
        error={
          <PostListMessage
            message="게시물을 불러오지 못했습니다."
            variant="destructive"
          />
        }
        end={
          <div className="py-4 text-center">
            <p className="text-muted-foreground text-xs">
              마지막 게시물입니다.
            </p>
          </div>
        }
      />
    </section>
  );
}
