'use client';

import {
  ClockIcon,
  GaugeIcon,
  QuestionIcon,
  StarIcon,
} from '@phosphor-icons/react';
import Link from 'next/link';

import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { RelativeTime } from '@/components/ui/relative-time';
import { Skeleton } from '@/components/ui/skeleton';
import { Unimplemented } from '@/components/ui/unimplemented';
import ROUTES from '@/util/routes';

// TODO: there's no knowledge-verification feature yet — placeholder stats
// until that backend work exists.
const MOCK_KNOWLEDGE_HEALTH = {
  freshPercent: 88,
  needsReviewCount: 4,
  unansweredCount: 3,
};

// TODO: there's no "pinned/canonical post" feature yet — placeholder cards
// until that exists.
const MOCK_PINNED_POSTS = [
  { title: '배포 파이프라인, 처음부터 끝까지', author: 'Priya', minutes: 12 },
  { title: '온콜 런북 & 에스컬레이션 경로', author: 'Dev', minutes: 8 },
];

interface ProjectDashboardProps {
  handle: string;
}

export default function ProjectDashboard({ handle }: ProjectDashboardProps) {
  const { data, isPending } = useGetProjectByHandle(handle);

  return (
    <div className="space-y-6">
      <KnowledgeHealthSection />
      <PinnedSection />
      <RecentActivitySection
        activities={data?.data.recentActivities ?? []}
        isPending={isPending}
        handle={handle}
      />
    </div>
  );
}

function KnowledgeHealthSection() {
  return (
    <Unimplemented>
      <Card>
        <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="flex items-center gap-3">
            <GaugeIcon className="text-success-text size-8" />
            <div>
              <p className="text-sm font-semibold">지식이 최신 상태예요</p>
              <p className="text-muted-foreground text-xs">
                최근 주기에서 {MOCK_KNOWLEDGE_HEALTH.freshPercent}% 검증됨
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <ClockIcon className="text-warning-text size-8" />
            <div>
              <p className="text-lg font-semibold">
                {MOCK_KNOWLEDGE_HEALTH.needsReviewCount}
              </p>
              <p className="text-muted-foreground text-xs">검토 필요</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <QuestionIcon className="text-destructive size-8" />
            <div>
              <p className="text-lg font-semibold">
                {MOCK_KNOWLEDGE_HEALTH.unansweredCount}
              </p>
              <p className="text-muted-foreground text-xs">미답변</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </Unimplemented>
  );
}

function PinnedSection() {
  return (
    <section className="space-y-3">
      <h2 className="flex items-center gap-1.5 text-sm font-semibold">
        <StarIcon />
        고정 · 정본
      </h2>

      <Unimplemented>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {MOCK_PINNED_POSTS.map((post) => (
            <Card key={post.title}>
              <CardContent className="space-y-2">
                <div className="flex items-center gap-2 text-xs">
                  <Badge variant="secondary">정본</Badge>
                  <span className="text-muted-foreground">가이드</span>
                </div>
                <p className="text-sm font-semibold">{post.title}</p>
                <p className="text-muted-foreground text-xs">
                  {post.author} · {post.minutes}분
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      </Unimplemented>
    </section>
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
  return (
    <section className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold">최근 활동</h2>
        <Link
          href={ROUTES.PROJECT_FEED(handle)}
          className="text-primary text-xs font-medium hover:underline"
        >
          피드에서 모두 보기 →
        </Link>
      </div>

      {isPending ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-14 w-full rounded-lg" />
          ))}
        </div>
      ) : activities.length === 0 ? (
        <p className="text-muted-foreground text-sm">최근 활동이 없습니다.</p>
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
