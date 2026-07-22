'use client';

import { PencilIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { toast } from 'sonner';

import {
  useGetProjectByHandle,
  useGetProjectTeammates,
} from '@/api/__generated__/project/project';
import {
  GetProjectResponseRole,
  GetProjectResponseSummaryJoinPolicy,
} from '@/api/__generated__/types';
import { ProjectAvatar } from '@/components/feature/project/avatar';
import {
  useFollowProject,
  useUnfollowProject,
} from '@/components/feature/project/hooks/useFollowProject';
import { useJoinProject } from '@/components/feature/project/hooks/useProjectJoinRequest';
import { Badge } from '@/components/ui/badge';
import { Button, LinkButton } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { useRequireAuth } from '@/hooks/use-require-auth';
import ROUTES from '@/util/routes';

// TODO: project creation date isn't returned by the API yet — derives a
// stable placeholder year from the handle until that field exists.
function mockCreatedYear(handle: string): number {
  let hash = 0;
  for (const char of handle) {
    hash = (hash * 31 + char.charCodeAt(0)) % 1000;
  }
  return 2022 + (hash % 4);
}

interface ProjectHeaderProps {
  handle: string;
}

export default function ProjectHeader({ handle }: ProjectHeaderProps) {
  const t = useTranslations('pages.projects.project.header');
  const { requireAuth } = useRequireAuth();

  const { data, isPending } = useGetProjectByHandle(handle);
  const { data: teammatesData } = useGetProjectTeammates(handle);

  const { mutate: followProject, isPending: isFollowPending } =
    useFollowProject();
  const { mutate: unfollowProject, isPending: isUnfollowPending } =
    useUnfollowProject();
  const { mutate: joinProject, isPending: isJoinPending } = useJoinProject();

  if (isPending || !data) {
    return <ProjectHeaderSkeleton />;
  }

  const { summary, role, hasPendingJoinRequest, isFollowing } = data.data;
  const memberCount = teammatesData?.data.teammates.length ?? 0;
  const isMember = !!role;

  const handleFollowToggle = () => {
    if (!requireAuth({ intent: 'follow' })) {
      return;
    }

    if (isFollowing) {
      unfollowProject({ handle });
      return;
    }

    followProject({ handle });
  };

  const handleJoin = () => {
    if (!requireAuth({ intent: 'join' })) {
      return;
    }

    joinProject(
      { handle },
      {
        onSuccess: () => {
          toast.success(
            summary.joinPolicy === GetProjectResponseSummaryJoinPolicy.Open
              ? t('join.joined')
              : t('join.requested'),
          );
        },
        onError: () => {
          toast.error(t('join.error'));
        },
      },
    );
  };

  const canJoin =
    summary.joinPolicy === GetProjectResponseSummaryJoinPolicy.Open ||
    summary.joinPolicy === GetProjectResponseSummaryJoinPolicy.Request;

  return (
    <Card>
      <CardContent className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-4">
          <ProjectAvatar
            name={summary.name}
            iconUrl={summary.iconUrl}
            size="lg"
            className="size-16 rounded-2xl text-2xl"
          />

          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-semibold">{summary.name}</h1>
              {role === GetProjectResponseRole.Admin && (
                <Badge variant="secondary">{t('role.admin')}</Badge>
              )}
            </div>

            <p className="text-muted-foreground text-sm">
              {summary.description || t('description-empty')}
            </p>

            <p className="text-muted-foreground text-xs">
              {t('meta', {
                count: memberCount,
                year: mockCreatedYear(handle),
              })}
            </p>
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-2">
          {/* Membership — joining the project is separate from following it. */}
          {isMember ? (
            <Button variant="outline" disabled>
              {t('status.member')}
            </Button>
          ) : (
            canJoin &&
            (hasPendingJoinRequest ? (
              <Button variant="outline" disabled>
                {t('join.requested-status')}
              </Button>
            ) : (
              <Button disabled={isJoinPending} onClick={handleJoin}>
                {summary.joinPolicy === GetProjectResponseSummaryJoinPolicy.Open
                  ? t('join.join')
                  : t('join.request')}
              </Button>
            ))
          )}

          {/* Following — for non-members only; membership already subscribes. */}
          {!isMember && (
            <Button
              variant="outline"
              disabled={isFollowPending || isUnfollowPending}
              onClick={handleFollowToggle}
            >
              {isFollowing ? t('follow.following') : t('follow.follow')}
            </Button>
          )}

          {/* Writing — teammates only. */}
          {isMember && (
            <LinkButton href={ROUTES.NEW_PROJECT_POST(handle)}>
              <PencilIcon />
              {t('actions.write')}
            </LinkButton>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function ProjectHeaderSkeleton() {
  return (
    <Card>
      <CardContent className="flex items-start gap-4">
        <Skeleton className="size-16 shrink-0 rounded-2xl" />
        <div className="flex-1 space-y-2">
          <Skeleton className="h-6 w-40" />
          <Skeleton className="h-4 w-64" />
          <Skeleton className="h-3 w-32" />
        </div>
      </CardContent>
    </Card>
  );
}
