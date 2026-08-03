'use client';

import {
  ClockIcon,
  GaugeIcon,
  QuestionIcon,
  StarIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import Link from 'next/link';

import { useGetPinnedPostsByProject } from '@/api/__generated__/post/post';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import type { GetPostsResponsePostsItem } from '@/api/__generated__/types';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { RelativeTime } from '@/components/ui/relative-time';
import { Skeleton } from '@/components/ui/skeleton';
import { Unimplemented } from '@/components/ui/unimplemented';
import ROUTES from '@/util/routes';

const WORDS_PER_MINUTE = 200;

// TODO: there's no knowledge-verification feature yet — placeholder stats
// until that backend work exists.
const MOCK_KNOWLEDGE_HEALTH = {
  freshPercent: 88,
  needsReviewCount: 4,
  unansweredCount: 3,
};

interface ProjectDashboardProps {
  handle: string;
}

export default function ProjectDashboard({ handle }: ProjectDashboardProps) {
  const { data, isPending } = useGetProjectByHandle(handle);

  return (
    <div className="space-y-6">
      <KnowledgeHealthSection />
      <PinnedSection handle={handle} />
      <RecentActivitySection
        activities={data?.data.recentActivities ?? []}
        isPending={isPending}
        handle={handle}
      />
    </div>
  );
}

function KnowledgeHealthSection() {
  const t = useTranslations(
    'pages.projects.project.dashboard.knowledge-health',
  );

  return (
    <Unimplemented>
      <Card>
        <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="flex items-center gap-3">
            <GaugeIcon className="text-success-text size-8" />
            <div>
              <p className="text-sm font-semibold">{t('fresh')}</p>
              <p className="text-muted-foreground text-xs">
                {t('fresh-detail', {
                  percent: MOCK_KNOWLEDGE_HEALTH.freshPercent,
                })}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <ClockIcon className="text-warning-text size-8" />
            <div>
              <p className="text-lg font-semibold">
                {MOCK_KNOWLEDGE_HEALTH.needsReviewCount}
              </p>
              <p className="text-muted-foreground text-xs">
                {t('needs-review')}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <QuestionIcon className="text-destructive size-8" />
            <div>
              <p className="text-lg font-semibold">
                {MOCK_KNOWLEDGE_HEALTH.unansweredCount}
              </p>
              <p className="text-muted-foreground text-xs">{t('unanswered')}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </Unimplemented>
  );
}

function PinnedSection({ handle }: { handle: string }) {
  const t = useTranslations('pages.projects.project.dashboard.pinned');
  const { data, isPending } = useGetPinnedPostsByProject(handle);
  const posts = data?.data.posts ?? [];

  if (!isPending && posts.length === 0) {
    return null;
  }

  return (
    <section className="space-y-3">
      <h2 className="flex items-center gap-1.5 text-sm font-semibold">
        <StarIcon />
        {t('heading')}
      </h2>

      {isPending ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {Array.from({ length: 2 }).map((_, index) => (
            <Skeleton key={index} className="h-20 w-full rounded-lg" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {posts.map((post) => (
            <PinnedPostCard key={post.id} handle={handle} post={post} t={t} />
          ))}
        </div>
      )}
    </section>
  );
}

function PinnedPostCard({
  handle,
  post,
  t,
}: {
  handle: string;
  post: GetPostsResponsePostsItem;
  t: ReturnType<
    typeof useTranslations<'pages.projects.project.dashboard.pinned'>
  >;
}) {
  const minutes = Math.max(1, Math.ceil(post.wordCount / WORDS_PER_MINUTE));

  return (
    <Link href={ROUTES.PROJECT_POST(handle, post.slug)}>
      <Card>
        <CardContent className="space-y-2">
          <div className="flex items-center gap-2 text-xs">
            <Badge variant="secondary">{t('badge')}</Badge>
          </div>
          <p className="text-sm font-semibold">{post.title ?? post.preview}</p>
          <p className="text-muted-foreground text-xs">
            {t('meta', { author: post.author.name, minutes })}
          </p>
        </CardContent>
      </Card>
    </Link>
  );
}

function RecentActivitySection({
  activities,
  isPending,
  handle,
}: {
  activities: { id: string; text: string; timestamp: string }[];
  isPending: boolean;
  handle: string;
}) {
  const t = useTranslations('pages.projects.project.dashboard.recent-activity');

  return (
    <section className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold">{t('heading')}</h2>
        <Link
          href={ROUTES.PROJECT_FEED(handle)}
          className="text-primary text-xs font-medium hover:underline"
        >
          {t('view-all')}
        </Link>
      </div>

      {isPending ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-14 w-full rounded-lg" />
          ))}
        </div>
      ) : activities.length === 0 ? (
        <p className="text-muted-foreground text-sm">{t('empty')}</p>
      ) : (
        <div className="space-y-3">
          {activities.map((activity) => (
            <Card key={activity.id} size="sm">
              <CardContent className="flex items-center justify-between gap-3">
                <p className="text-sm">{activity.text}</p>
                <span className="text-muted-foreground shrink-0 text-xs">
                  <RelativeTime date={activity.timestamp} />
                </span>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </section>
  );
}
