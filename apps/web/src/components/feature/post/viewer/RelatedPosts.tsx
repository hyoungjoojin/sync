'use client';

import { useTranslations } from 'next-intl';

import {
  useGetPostBySlug,
  useGetRelatedPosts,
} from '@/api/__generated__/post/post';
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from '@/components/ui/carousel';

import { PostPreviewCard } from './PostCard';
import { type PostSummary, toPostSummary } from './types';

const CAROUSEL_OPTS = { align: 'start' } as const;

interface RelatedPostsProps {
  slug: string;
}

export function RelatedPosts({ slug }: RelatedPostsProps) {
  // 상세 페이지가 이미 프리페치/하이드레이트한 게시물 캐시에서 postId를 얻는다.
  const { data: post } = useGetPostBySlug(slug);
  const postId = post?.data.summary.id;

  const { data: related } = useGetRelatedPosts(String(postId ?? ''), {
    query: { enabled: postId != null },
  });

  const posts = related?.data.posts ?? [];

  // 관련 게시물이 없으면 섹션 전체를 렌더링하지 않는다.
  if (posts.length === 0) {
    return null;
  }

  const summaries = posts.map((item) => toPostSummary(item));

  return (
    <section className="mt-6 space-y-4">
      <RelatedPostsGrid summaries={summaries} />
      <RelatedPostsCarousel summaries={summaries} />
    </section>
  );
}

function RelatedPostsHeading() {
  const t = useTranslations('components.post.viewer.related');

  return <h2 className="text-lg font-semibold">{t('title')}</h2>;
}

/** 데스크톱(lg 이상): 캐러셀 없이 최대 5개를 한 행에 나란히 표시한다. */
function RelatedPostsGrid({ summaries }: { summaries: PostSummary[] }) {
  return (
    <div className="hidden space-y-4 lg:block">
      <RelatedPostsHeading />
      <div className="grid grid-cols-5 items-stretch gap-4">
        {summaries.map((summary) => (
          <PostPreviewCard key={summary.id} summary={summary} fillHeight />
        ))}
      </div>
    </div>
  );
}

/** 모바일(lg 미만): 한 번에 하나씩 보여주는 캐러셀. */
function RelatedPostsCarousel({ summaries }: { summaries: PostSummary[] }) {
  return (
    <Carousel opts={CAROUSEL_OPTS} className="space-y-4 lg:hidden">
      <div className="flex items-center justify-between">
        <RelatedPostsHeading />
        <div className="flex gap-2">
          <CarouselPrevious className="static" />
          <CarouselNext className="static" />
        </div>
      </div>

      <CarouselContent className="items-stretch">
        {summaries.map((summary) => (
          <CarouselItem key={summary.id} className="basis-full">
            <PostPreviewCard summary={summary} fillHeight />
          </CarouselItem>
        ))}
      </CarouselContent>
    </Carousel>
  );
}
