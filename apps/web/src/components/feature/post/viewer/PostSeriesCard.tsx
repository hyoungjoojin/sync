'use client';

import { CaretDownIcon, CaretUpIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import { useGetSeriesForPost } from '@/api/__generated__/post-series/post-series';
import { useGetPostBySlug } from '@/api/__generated__/post/post';
import { useReorderSeriesPost } from '@/components/feature/post/hooks/useReorderSeriesPost';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { cn } from '@/lib/utils';

interface PostSeriesCardProps {
  slug: string;
}

/**
 * 상세 페이지의 댓글 위에 놓이는 시리즈 카드. 시리즈 정보는 게시글 상세 응답이 아니라
 * `GET /posts/{slug}/series` 에서 직접 가져온다. 게시글이 시리즈에 속하지 않으면
 * (`series` 가 없으면) 아무것도 렌더링하지 않는다. 속해 있으면 시리즈 이름과
 * "N번째 / 전체 M편", 그리고 순서대로 나열된 편 목록을 보여주고, 저자에게는
 * 현재 글의 순서를 위/아래로 옮기는 버튼을 제공한다.
 */
export function PostSeriesCard({ slug }: PostSeriesCardProps) {
  const t = useTranslations('components.post.viewer.series');
  const { data: post } = useGetPostBySlug(slug);
  const isAuthor = post?.data.summary.isAuthor ?? false;

  const { data: seriesData } = useGetSeriesForPost(slug);
  const series = seriesData?.data.series;
  const seriesId = series?.externalId;
  const currentSeriesPostId = seriesData?.data.currentSeriesPostId;
  const reorder = useReorderSeriesPost(slug);

  if (!series || !seriesId) {
    return null;
  }

  const items = seriesData?.data.posts ?? [];
  const lastPosition = items.reduce(
    (max, item) => Math.max(max, item.position),
    0,
  );
  // 순서는 목록에서 직접 찾는다. denormalize 된 postCount 대신 실제 항목 수를 쓴다.
  const position =
    items.find((item) => item.seriesPostId === currentSeriesPostId)?.position ??
    0;

  const move = (seriesPostId: number, targetPosition: number) => {
    reorder.mutate({
      externalId: seriesId,
      seriesPostId: String(seriesPostId),
      data: { position: targetPosition },
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardDescription>{t('label')}</CardDescription>
        <CardTitle className="text-base">{series.name}</CardTitle>
        <CardDescription>
          {t('part', { position, total: items.length })}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <ol className="flex flex-col gap-0.5">
          {items.map((item) => {
            const current = item.seriesPostId === currentSeriesPostId;
            return (
              <li
                key={item.seriesPostId}
                className={cn(
                  'flex items-center gap-2 rounded-md px-2 py-1.5 text-sm',
                  current && 'bg-accent font-medium',
                )}
              >
                <span className="w-5 shrink-0 text-right tabular-nums text-muted-foreground">
                  {item.position}
                </span>
                <span
                  className={cn(
                    'min-w-0 flex-1 truncate',
                    !item.title && 'italic text-muted-foreground',
                  )}
                >
                  {item.title ?? t('hidden')}
                </span>
                {isAuthor && (
                  <span className="flex shrink-0 items-center">
                    <button
                      type="button"
                      aria-label={t('move-up')}
                      disabled={item.position <= 1 || reorder.isPending}
                      onClick={() => move(item.seriesPostId, item.position - 1)}
                      className="rounded p-0.5 text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-30 disabled:hover:bg-transparent"
                    >
                      <CaretUpIcon size={14} />
                    </button>
                    <button
                      type="button"
                      aria-label={t('move-down')}
                      disabled={
                        item.position >= lastPosition || reorder.isPending
                      }
                      onClick={() => move(item.seriesPostId, item.position + 1)}
                      className="rounded p-0.5 text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-30 disabled:hover:bg-transparent"
                    >
                      <CaretDownIcon size={14} />
                    </button>
                  </span>
                )}
              </li>
            );
          })}
        </ol>
      </CardContent>
    </Card>
  );
}
