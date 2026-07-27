import { HydrationBoundary, dehydrate } from '@tanstack/react-query';
import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { notFound, redirect } from 'next/navigation';

import { getGetPostCommentsInfiniteQueryOptions } from '@/api/__generated__/comment/comment';
import { getGetPostBySlugQueryKey } from '@/api/__generated__/post/post';
import { COMMENT_PAGE_SIZE } from '@/components/feature/post/constants';
import type { PostType } from '@/components/feature/post/types/post';
import { PostCard } from '@/components/feature/post/viewer/PostCard';
import PostComments from '@/components/feature/post/viewer/PostComments';
import { PostSeriesCard } from '@/components/feature/post/viewer/PostSeriesCard';
import { RelatedPosts } from '@/components/feature/post/viewer/RelatedPosts';
import { TwoColumnLayout } from '@/components/layout/TwoColumnLayout';
import SyncError, { ErrorCode } from '@/lib/error';
import { getPostBySlugCached } from '@/lib/post-query';
import { getQueryClient } from '@/lib/query';
import {
  NON_INDEXABLE_METADATA,
  buildPostJsonLd,
  createPostMetadata,
  isPostIndexable,
} from '@/lib/seo';
import ROUTES from '@/util/routes';

interface PostProps {
  params: Promise<{
    slug: string;
  }>;
}

export async function generateMetadata({
  params,
}: PostProps): Promise<Metadata> {
  const { slug } = await params;
  const t = await getTranslations('metadata');

  try {
    const { data: post } = await getPostBySlugCached(slug);

    // 프로젝트 게시물은 프로젝트 경로가 정규 URL이며,
    // createPostMetadata가 post.project.handle을 보고 canonical을 계산한다.
    return createPostMetadata(post.summary, t('description'));
  } catch {
    return NON_INDEXABLE_METADATA;
  }
}

export default async function Post({ params }: PostProps) {
  const { slug } = await params;

  const queryClient = getQueryClient();
  let commentsEnabled = false;
  let postType: PostType | undefined;
  let isPostAuthor = false;
  let jsonLd: Record<string, unknown> | null = null;

  try {
    const response = await getPostBySlugCached(slug);
    const post = response.data;

    queryClient.setQueryData(getGetPostBySlugQueryKey(slug), response);

    commentsEnabled = post.summary.status === 'PUBLISHED';
    postType = post.summary.type as PostType;
    isPostAuthor = post.summary.isAuthor;

    if (post.summary.project?.handle) {
      redirect(ROUTES.PROJECT_POST(post.summary.project.handle, slug));
    }

    if (isPostIndexable(post.summary)) {
      jsonLd = buildPostJsonLd(post.summary, ROUTES.POST(slug));
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
          <div className="flex flex-col gap-6">
            <PostSeriesCard slug={slug} />
            {commentsEnabled && postType ? (
              <PostComments
                slug={slug}
                postType={postType}
                isPostAuthor={isPostAuthor}
              />
            ) : null}
          </div>
        }
      />
      <RelatedPosts slug={slug} />
    </HydrationBoundary>
  );
}
