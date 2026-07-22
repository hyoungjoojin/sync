'use client';

import type { Editor } from '@tiptap/react';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';

import { useGetPostBySlug } from '@/api/__generated__/post/post';
import { ProfileHoverCard } from '@/components/feature/profile/ProfileHoverCard';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

import { PostType } from '../types/post';
import { PostBody } from './components/PostBody';
import { PostCardActions } from './components/PostCardActions';
import { PostPreviewBody } from './components/PostPreviewBody';
import { PostTagChips } from './components/PostTagChips';
import { PostViewHeader } from './components/PostViewHeader';
import PostRenderErrorBoundary from './error/PostRenderErrorBoundary';
import { useReadOnlyPostEditor } from './hooks/useReadOnlyPostEditor';
import {
  type PostPreviewMedia,
  type PostProjectSummary,
  type PostSummary,
  type PostViewSource,
  toPostViewSource,
} from './types';
import { normalizePostContent } from './utils/normalizePostContent';

const WORDS_PER_MINUTE = 200;

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
  const editor = useReadOnlyPostEditor(normalizePostContent(content));
  const postPath = summary.project?.handle
    ? ROUTES.PROJECT_POST(summary.project.handle, summary.slug)
    : ROUTES.POST(summary.slug);

  // 유료 게이트로 본문이 빠진 응답에서는 요약의 미리보기 텍스트로 대체한다.
  const lockedPreview = content === undefined ? summary.preview : undefined;

  const typeCardProps: TypePostCardProps = {
    summary,
    editor,
    postPath,
    lockedPreview,
  };

  switch (summary.type) {
    case PostType.QUESTION:
      return <QuestionTypePostCard {...typeCardProps} />;
    case PostType.LONG:
      return <LongTypePostCard {...typeCardProps} />;
    case PostType.SHORT:
    default:
      return <ShortTypePostCard {...typeCardProps} />;
  }
}

interface TypePostCardProps {
  summary: PostSummary;
  editor: Editor | null;
  postPath: string;
  lockedPreview?: string;
}

function ShortTypePostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: TypePostCardProps) {
  return (
    <Card>
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-3">
        {summary.title && (
          <h3 className="text-lg font-semibold">{summary.title}</h3>
        )}
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions
          postId={summary.id}
          liked={summary.liked}
          likeCount={summary.likeCount}
          commentCount={summary.commentCount}
          bookmarked={summary.bookmarked}
        />
        <PostTagChips tags={summary.tags} />
      </CardContent>
    </Card>
  );
}

function LongTypePostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: TypePostCardProps) {
  return (
    <Card>
      {summary.coverImageUrl && (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={summary.coverImageUrl}
          alt=""
          className="aspect-[2.5/1] w-full object-cover"
        />
      )}
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-4">
        {summary.title && (
          <h3 className="text-lg font-semibold">{summary.title}</h3>
        )}
        <PostBody
          editor={editor}
          className="line-clamp-4"
          lockedPreview={lockedPreview}
        />
        <PostCardActions
          postId={summary.id}
          liked={summary.liked}
          likeCount={summary.likeCount}
          commentCount={summary.commentCount}
          bookmarked={summary.bookmarked}
        />
        <PostTagChips tags={summary.tags} />
      </CardContent>
    </Card>
  );
}

function QuestionTypePostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: TypePostCardProps) {
  return (
    <Card>
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-4">
        {summary.title && (
          <h3 className="text-lg font-semibold">{summary.title}</h3>
        )}
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions
          postId={summary.id}
          liked={summary.liked}
          likeCount={summary.likeCount}
          commentCount={summary.commentCount}
          bookmarked={summary.bookmarked}
        />
        <PostTagChips tags={summary.tags} />
      </CardContent>
    </Card>
  );
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
}

export function PostPreviewCard({ summary, fillHeight }: PostPreviewCardProps) {
  return (
    <PostRenderErrorBoundary>
      <PostPreviewCardBySummary summary={summary} fillHeight={fillHeight} />
    </PostRenderErrorBoundary>
  );
}

function PostPreviewCardBySummary({
  summary,
  fillHeight,
}: PostPreviewCardProps) {
  const router = useRouter();

  const postPath = summary.project?.handle
    ? ROUTES.PROJECT_POST(summary.project.handle, summary.slug)
    : ROUTES.POST(summary.slug);

  const typePreviewCardProps: TypePostPreviewCardProps = {
    summary,
    postPath,
    fillHeight,
    onClick: () => router.push(postPath),
  };

  switch (summary.type) {
    case PostType.QUESTION:
      return <QuestionTypePostPreviewCard {...typePreviewCardProps} />;
    case PostType.LONG:
      return <LongTypePostPreviewCard {...typePreviewCardProps} />;
    case PostType.SHORT:
    default:
      return <ShortTypePostPreviewCard {...typePreviewCardProps} />;
  }
}

interface TypePostPreviewCardProps {
  summary: PostSummary;
  postPath: string;
  onClick: () => void;
  fillHeight?: boolean;
}

function ShortTypePostPreviewCard({
  summary,
  postPath,
  onClick,
  fillHeight,
}: TypePostPreviewCardProps) {
  return (
    <Card onClick={onClick} className={cn(fillHeight && 'h-full')}>
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="preview"
        />
      </CardHeader>

      <CardContent className="space-y-4">
        {summary.title && (
          <h3 className="text-lg font-semibold">{summary.title}</h3>
        )}
        <PostPreviewBody preview={summary.preview} />

        <div className="flex items-center justify-end">
          <PostCardActions
            postId={summary.id}
            liked={summary.liked}
            likeCount={summary.likeCount}
            commentCount={summary.commentCount}
            bookmarked={summary.bookmarked}
          />
        </div>

        <PostTagChips tags={summary.tags} />
      </CardContent>
    </Card>
  );
}

function LongTypePostPreviewCard({
  summary,
  onClick,
  fillHeight,
}: TypePostPreviewCardProps) {
  const stopPropagation = (event: React.MouseEvent) => event.stopPropagation();

  return (
    <Card
      onClick={onClick}
      className={cn('overflow-hidden py-0', fillHeight && 'h-full')}
    >
      <ArticlePreviewMedia
        previewMedia={summary.previewMedia}
        wordCount={summary.wordCount}
        project={summary.project}
        coverImageUrl={summary.coverImageUrl}
      />

      <CardContent className="space-y-4 py-4">
        {summary.title && (
          <h3 className="text-lg font-semibold">{summary.title}</h3>
        )}
        <PostPreviewBody preview={summary.preview} className="line-clamp-6" />

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div onClick={stopPropagation}>
              <ProfileHoverCard
                handle={summary.author.handle}
                name={summary.author.name}
                size="sm"
              />
            </div>
            <span className="text-sm font-medium">{summary.author.name}</span>
          </div>

          <PostCardActions
            postId={summary.id}
            liked={summary.liked}
            likeCount={summary.likeCount}
            commentCount={summary.commentCount}
            bookmarked={summary.bookmarked}
            variant="bookmark-only"
          />
        </div>

        <PostTagChips tags={summary.tags} />
      </CardContent>
    </Card>
  );
}

function QuestionTypePostPreviewCard({
  summary,
  postPath,
  onClick,
  fillHeight,
}: TypePostPreviewCardProps) {
  const t = useTranslations('components.post.viewer');

  return (
    <Card onClick={onClick} className={cn(fillHeight && 'h-full')}>
      <CardContent className="flex gap-4">
        <div className="flex w-14 shrink-0 flex-col items-center gap-2 text-center">
          <div>
            <p className="text-lg font-semibold">{summary.likeCount}</p>
            <p className="text-muted-foreground text-xs">{t('votes')}</p>
          </div>
          <div className="rounded-md border px-2 py-1">
            <p className="text-sm font-semibold">{summary.commentCount}</p>
            <p className="text-muted-foreground text-xs">{t('answers')}</p>
          </div>
        </div>

        <div className="min-w-0 flex-1 space-y-3">
          <PostViewHeader
            summary={summary}
            postPath={postPath}
            variant="preview"
          />

          {summary.resolved && (
            <span className="text-xs font-medium text-success-text">
              {t('answered')}
            </span>
          )}

          {summary.title && (
            <h3 className="text-lg font-semibold">{summary.title}</h3>
          )}
          <PostPreviewBody preview={summary.preview} />

          <div className="flex items-center justify-between">
            <PostCardActions
              postId={summary.id}
              liked={summary.liked}
              likeCount={summary.likeCount}
              commentCount={summary.commentCount}
              bookmarked={summary.bookmarked}
            />
            <PostTagChips tags={summary.tags} />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function ArticlePreviewMedia({
  previewMedia,
  wordCount,
  project,
  coverImageUrl,
}: {
  previewMedia: PostPreviewMedia[];
  wordCount: number;
  project?: PostProjectSummary;
  coverImageUrl?: string | null;
}) {
  const t = useTranslations('components.post.viewer');
  const readingMinutes = Math.max(1, Math.ceil(wordCount / WORDS_PER_MINUTE));
  // TODO: no category field on posts yet — falls back to the project name,
  // or a generic label.
  const category = project?.name?.toUpperCase() ?? t('article');
  // The author-picked cover takes precedence over the first inline image.
  const backgroundUrl = coverImageUrl ?? previewMedia[0]?.url;

  return (
    <div
      className="relative flex h-40 items-end bg-gradient-to-br from-primary/20 to-success-tint bg-cover bg-center p-4"
      style={
        backgroundUrl ? { backgroundImage: `url(${backgroundUrl})` } : undefined
      }
    >
      <span className="absolute top-3 right-3 rounded-full bg-background/80 px-2 py-0.5 text-xs font-medium">
        {t('minRead', { minutes: readingMinutes })}
      </span>
      <span className="text-xs font-semibold tracking-wide text-primary">
        {category}
      </span>
    </div>
  );
}
