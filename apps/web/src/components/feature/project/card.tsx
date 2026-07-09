import { CheckCircleIcon, ClockIcon } from '@phosphor-icons/react';

import { Badge } from '@/components/ui/badge';
import { LinkButton } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import ROUTES from '@/util/routes';

import { ProjectAvatar } from './avatar';

interface ProjectCardProps {
  name: string;
  handle: string;
  iconUrl?: string | null;
  description?: string | null;
  role?: 'admin' | 'member';
  memberCount?: number;
  freshPercent?: number;
  toReviewCount?: number;
  unansweredCount?: number;
}

function ProjectCard({
  name,
  handle,
  iconUrl,
  description,
  role,
  memberCount,
  freshPercent,
  toReviewCount,
  unansweredCount,
}: ProjectCardProps) {
  const isFresh = freshPercent !== undefined && freshPercent >= 85;

  return (
    <Card className="gap-4 p-5">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <ProjectAvatar
            name={name}
            iconUrl={iconUrl}
            size="lg"
            className="size-10 text-lg"
          />
          <div className="min-w-0">
            <p className="truncate font-semibold">{name}</p>
            {memberCount !== undefined && (
              <p className="text-muted-foreground text-xs">
                멤버 {memberCount}명
              </p>
            )}
          </div>
        </div>

        {role && (
          <Badge variant="outline" className="shrink-0 font-normal">
            {role === 'admin' ? '관리자' : '멤버'}
          </Badge>
        )}
      </div>

      {description && (
        <p className="text-muted-foreground line-clamp-2 text-sm">
          {description}
        </p>
      )}

      {freshPercent !== undefined &&
        toReviewCount !== undefined &&
        unansweredCount !== undefined && (
          <div className="border-hairline flex items-center justify-between border-t pt-4 text-xs">
            <div className="flex items-center gap-1">
              {isFresh ? (
                <CheckCircleIcon weight="fill" className="text-success-text" />
              ) : (
                <ClockIcon weight="fill" className="text-warning-text" />
              )}
              <span
                className={isFresh ? 'text-success-text' : 'text-warning-text'}
              >
                {freshPercent}%
              </span>
              <span className="text-muted-foreground">최신</span>
            </div>

            <div className="text-muted-foreground">
              <span className="text-foreground font-medium">
                {toReviewCount}
              </span>{' '}
              검토 대기
            </div>

            <div className="text-muted-foreground">
              <span className="text-foreground font-medium">
                {unansweredCount}
              </span>{' '}
              미답변
            </div>
          </div>
        )}

      <LinkButton
        href={ROUTES.PROJECT(handle)}
        size="sm"
        variant="secondary"
        className="w-full"
      >
        프로젝트 열기
      </LinkButton>
    </Card>
  );
}

export { ProjectCard };
