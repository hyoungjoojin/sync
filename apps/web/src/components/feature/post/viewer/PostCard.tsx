'use client';

import { useRouter } from 'next/navigation';

import { useGetPostBySlug } from '@/api/__generated__/post/post';
import { Skeleton } from '@/components/ui/skeleton';
import ROUTES from '@/util/routes';

import { PostType } from '../types/post';
import { LongPostCard } from './cards/LongPostCard';
import { LongPostPreviewCard } from './cards/LongPostPreviewCard';
import { QuestionPostCard } from './cards/QuestionPostCard';
import { QuestionPostPreviewCard } from './cards/QuestionPostPreviewCard';
import { ShortPostCard } from './cards/ShortPostCard';
import { ShortPostPreviewCard } from './cards/ShortPostPreviewCard';
import type {
  PostDetailCardProps,
  PostPreviewCardTypeProps,
  PostPreviewSurface,
} from './cards/types';
import PostRenderErrorBoundary from './error/PostRenderErrorBoundary';
import { useReadOnlyPostEditor } from './hooks/useReadOnlyPostEditor';
import {
  type PostSummary,
  type PostViewSource,
  toPostViewSource,
} from './types';
import { normalizePostContent } from './utils/normalizePostContent';

function resolvePostPath(summary: PostSummary) {
  return summary.project?.handle
    ? ROUTES.PROJECT_POST(summary.project.handle, summary.slug)
    : ROUTES.POST(summary.slug);
}

// ---- PostCard: detail view, loads its own data by slug ----

interface PostCardProps {
  slug: string;
}

export function PostCard({ slug }: PostCardProps) {
  const { data, isPending } = useGetPostBySlug(slug);

  if (isPending || !data) {
    return <Skeleton className="h-40 w-full" />;
  }

  return (
    <PostRenderErrorBoundary>
      <PostCardBySource source={toPostViewSource(data.data)} />
    </PostRenderErrorBoundary>
  );
}

function PostCardBySource({ source }: { source: PostViewSource }) {
  const { summary, content } = source;
  const editor = useReadOnlyPostEditor(
    normalizePostContent(content),
    summary.slug,
  );

  const props: PostDetailCardProps = {
    summary,
    editor,
    postPath: resolvePostPath(summary),
    // 유료 게이트로 본문이 빠진 응답에서는 요약의 미리보기 텍스트로 대체한다.
    lockedPreview: content === undefined ? summary.preview : undefined,
  };

  switch (summary.type) {
    case PostType.QUESTION:
      return <QuestionPostCard {...props} />;
    case PostType.LONG:
      return <LongPostCard {...props} />;
    case PostType.SHORT:
    default:
      return <ShortPostCard {...props} />;
  }
}

// ---- PostPreviewCard: feed view, data injected by PostList ----

export interface PostPreviewCardProps {
  summary: PostSummary;
  /**
   * 카드가 부모의 높이를 가득 채우도록 한다(`h-full`). 관련 게시물처럼 여러 카드를
   * 한 행/캐러셀에 나란히 배치해 높이를 맞춰야 할 때 사용한다. 부모가
   * stretch(그리드/flex 기본값)일 때 모든 카드가 가장 큰 카드 높이에 맞춰진다.
   */
  fillHeight?: boolean;
  /** 목록에 이어 붙는 자리라면 `flat`. 기본값은 낱개로 떠 있는 `card`. */
  surface?: PostPreviewSurface;
}

export function PostPreviewCard({
  summary,
  fillHeight,
  surface = 'card',
}: PostPreviewCardProps) {
  return (
    <PostRenderErrorBoundary>
      <PostPreviewCardBySummary
        summary={summary}
        fillHeight={fillHeight}
        surface={surface}
      />
    </PostRenderErrorBoundary>
  );
}

function PostPreviewCardBySummary({
  summary,
  fillHeight,
  surface = 'card',
}: PostPreviewCardProps) {
  const router = useRouter();
  const postPath = resolvePostPath(summary);

  const props: PostPreviewCardTypeProps = {
    summary,
    postPath,
    fillHeight,
    surface,
    onClick: () => router.push(postPath),
  };

  switch (summary.type) {
    case PostType.QUESTION:
      return <QuestionPostPreviewCard {...props} />;
    case PostType.LONG:
      return <LongPostPreviewCard {...props} />;
    case PostType.SHORT:
    default:
      return <ShortPostPreviewCard {...props} />;
  }
}
