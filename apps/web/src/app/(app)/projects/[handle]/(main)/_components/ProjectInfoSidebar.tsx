'use client';

import Link from 'next/link';

import {
  useGetFollowedProjects,
  useGetProjectByHandle,
  useGetProjectTeammates,
} from '@/api/__generated__/project/project';
import { ProjectAvatar } from '@/components/feature/project/avatar';
import {
  useFollowProject,
  useUnfollowProject,
} from '@/components/feature/project/hooks/useFollowProject';
import {
  Avatar,
  AvatarFallback,
  AvatarGroup,
  AvatarImage,
} from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import { useRequireAuth } from '@/hooks/use-require-auth';
import { isAuthenticated } from '@/lib/auth';
import { useSession } from '@/lib/auth/client';
import ROUTES from '@/util/routes';

const MAX_VISIBLE_TEAMMATES = 5;

interface ProjectInfoSidebarProps {
  handle: string;
}

export default function ProjectInfoSidebar({
  handle,
}: ProjectInfoSidebarProps) {
  const { data: session } = useSession();
  const { requireAuth } = useRequireAuth();

  const { data, isPending } = useGetProjectByHandle(handle);
  const { data: teammatesData } = useGetProjectTeammates(handle);

  const { data: followedProjectsData } = useGetFollowedProjects(
    session?.user.handle || '',
    {
      query: {
        enabled: isAuthenticated(session),
      },
    },
  );

  const { mutate: followProject, isPending: isFollowPending } =
    useFollowProject();
  const { mutate: unfollowProject, isPending: isUnfollowPending } =
    useUnfollowProject();

  if (isPending) {
    return <ProjectInfoSidebarSkeleton />;
  }

  if (!data) {
    return null;
  }

  const { summary } = data.data;

  const teammates = teammatesData?.data.teammates ?? [];
  const visibleTeammates = teammates.slice(0, MAX_VISIBLE_TEAMMATES);
  const hiddenTeammatesCount = teammates.length - visibleTeammates.length;

  const isFollowing =
    followedProjectsData?.data.projects.some((p) => p.handle === handle) ??
    false;

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

  return (
    <div className="flex flex-col items-center gap-4 rounded-lg border p-6 text-center">
      <ProjectAvatar
        name={summary.name}
        iconUrl={summary.iconUrl}
        size="lg"
        className="size-16 rounded-2xl text-2xl"
      />

      <div>
        <p className="text-lg font-semibold">{summary.name}</p>
        <p className="text-muted-foreground text-sm">@{summary.handle}</p>
      </div>

      <p className="text-sm text-muted-foreground">
        {summary.description || '설명이 없습니다.'}
      </p>

      {teammates.length > 0 && (
        <Link
          href={ROUTES.PROJECT_SETTINGS_TEAMMATES(handle)}
          className="flex flex-col items-center gap-2"
        >
          <p className="text-xs font-medium text-muted-foreground">
            팀원 {teammates.length}
          </p>
          <AvatarGroup>
            {visibleTeammates.map(({ user }) => (
              <Tooltip key={user.handle}>
                <TooltipTrigger asChild>
                  <Avatar size="sm">
                    <AvatarImage
                      src={user.profileImageUrl ?? undefined}
                      alt={user.name}
                    />
                    <AvatarFallback>{user.name.charAt(0)}</AvatarFallback>
                  </Avatar>
                </TooltipTrigger>
                <TooltipContent>{user.name}</TooltipContent>
              </Tooltip>
            ))}
            {hiddenTeammatesCount > 0 && (
              <div className="bg-muted text-muted-foreground ring-background relative flex size-6 shrink-0 items-center justify-center rounded-full text-xs ring-2">
                +{hiddenTeammatesCount}
              </div>
            )}
          </AvatarGroup>
        </Link>
      )}

      <Button
        className="w-full"
        variant={isFollowing ? 'outline' : 'default'}
        disabled={isFollowPending || isUnfollowPending}
        onClick={handleFollowToggle}
      >
        {isFollowing ? '팔로잉' : '팔로우'}
      </Button>
    </div>
  );
}

function ProjectInfoSidebarSkeleton() {
  return (
    <div className="flex flex-col items-center gap-4 rounded-lg border p-6">
      <Skeleton className="size-16 rounded-2xl" />
      <div className="flex flex-col items-center gap-2">
        <Skeleton className="h-5 w-32" />
        <Skeleton className="h-4 w-20" />
      </div>
      <Skeleton className="h-4 w-full" />
      <div className="flex -space-x-2">
        {Array.from({ length: 5 }).map((_, index) => (
          <Skeleton
            key={index}
            className="ring-background size-6 rounded-full ring-2"
          />
        ))}
      </div>
      <Skeleton className="h-9 w-full" />
    </div>
  );
}
