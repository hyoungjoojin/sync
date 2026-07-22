import { HydrationBoundary, dehydrate } from '@tanstack/react-query';
import { Metadata } from 'next';
import { notFound } from 'next/navigation';

import { getGetPostCommentsInfiniteQueryOptions } from '@/api/__generated__/comment/comment';
import {
  getGetPostBySlugQueryOptions,
  getPostBySlug,
} from '@/api/__generated__/post/post';
import { COMMENT_PAGE_SIZE } from '@/components/feature/post/constants';
import type { PostType } from '@/components/feature/post/types/post';
import { PostCard } from '@/components/feature/post/viewer/PostCard';
import PostComments from '@/components/feature/post/viewer/PostComments';
import { PostSeriesCard } from '@/components/feature/post/viewer/PostSeriesCard';
import { RelatedPosts } from '@/components/feature/post/viewer/RelatedPosts';
import { TwoColumnLayout } from '@/components/layout/TwoColumnLayout';
import SyncError, { ErrorCode } from '@/lib/error';
import { getQueryClient } from '@/lib/query';
import { buildPostJsonLd, buildPostMetadata, isPostIndexable } from '@/lib/seo';
import ROUTES from '@/util/routes';

interface PostProps {
  params: Promise<{
    handle: string;
    slug: string;
  }>;
}

export async function generateMetadata({
  params,
}: PostProps): Promise<Metadata> {
  const { handle, slug } = await params;

  try {
    const { data: post } = await getPostBySlug(slug);

    return buildPostMetadata(post.summary, ROUTES.PROJECT_POST(handle, slug));
  } catch {
    return {};
  }
}

export default async function Post({ params }: PostProps) {
  const { handle, slug } = await params;

  const queryClient = getQueryClient();
  let commentsEnabled = false;
  let postType: PostType | undefined;
  let isPostAuthor = false;
  let jsonLd: Record<string, unknown> | null = null;

  try {
    const { data: post } = await queryClient.fetchQuery(
      getGetPostBySlugQueryOptions(slug),
    );
    commentsEnabled = post.summary.status === 'PUBLISHED';
    postType = post.summary.type as PostType;
    isPostAuthor = post.summary.isAuthor;

    if (isPostIndexable(post.summary)) {
      jsonLd = buildPostJsonLd(post.summary, ROUTES.PROJECT_POST(handle, slug));
    }
  } catch (error) {
    if (error instanceof SyncError) {
      switch (error.code) {
        case ErrorCode.POST_NOT_FOUND:
          notFound();
      }
    }

    throw error;
  }

  if (commentsEnabled) {
    await queryClient.prefetchInfiniteQuery(
      getGetPostCommentsInfiniteQueryOptions(
        slug,
        { first: COMMENT_PAGE_SIZE },
        {
          query: {
            getNextPageParam: (lastPage) => {
              const pageInfo = lastPage.data.comments?.pageInfo;
              return pageInfo?.hasNextPage
                ? (pageInfo.endCursor ?? undefined)
                : undefined;
            },
          },
        },
      ),
    );
  }

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      {jsonLd && (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      )}
      <TwoColumnLayout
        main={<PostCard slug={slug} />}
        side={
          <>
            <PostSeriesCard slug={slug} />
            {commentsEnabled && postType ? (
              <PostComments
                slug={slug}
                postType={postType}
                isPostAuthor={isPostAuthor}
              />
            ) : null}
          </>
        }
      />
      <RelatedPosts slug={slug} />
    </HydrationBoundary>
  );
}
