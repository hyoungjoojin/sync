'use client';

import Link from 'next/link';

import { ProfileHoverCard } from '@/components/feature/profile/ProfileHoverCard';
import { ProjectHoverCard } from '@/components/feature/project/ProjectHoverCard';
import { RelativeTime } from '@/components/ui/relative-time';
import { useProjectContextHandle } from '@/hooks/use-project-context';
import ROUTES from '@/util/routes';

import type {
  PostAuthorSummary,
  PostProjectSummary,
  PostSummary,
} from '../types';
import { DraftedViaBadge } from './DraftedViaBadge';

type IdentifiedProject = PostProjectSummary & { handle: string };

/**
 * 게시물의 주체를 정한다. 프로젝트 글을 그 프로젝트 밖에서 보면 프로젝트를,
 * 프로젝트 안에서 보면 이미 어느 프로젝트인지 아니까 작성자를 보여준다.
 * 맥락 판별은 사이드바가 개인/프로젝트 모드를 고르는 규칙과 같다.
 */
export function PostHeaderIdentity({
  summary,
  isPreview,
}: {
  summary: PostSummary;
  isPreview: boolean;
}) {
  const contextHandle = useProjectContextHandle();
  const { project } = summary;

  if (project?.handle && project.handle !== contextHandle) {
    return (
      <ProjectIdentity
        project={{ ...project, handle: project.handle }}
        createdAt={summary.createdAt}
        isPreview={isPreview}
        createdViaClientName={summary.createdViaClientName}
      />
    );
  }

  return (
    <AuthorIdentity
      author={summary.author}
      createdAt={summary.createdAt}
      isPreview={isPreview}
      createdViaClientName={summary.createdViaClientName}
    />
  );
}

function ProjectIdentity({
  project,
  createdAt,
  isPreview,
  createdViaClientName,
}: {
  project: IdentifiedProject;
  createdAt: string;
  isPreview: boolean;
  createdViaClientName?: string | null;
}) {
  const name = project.name ?? project.handle;

  return (
    <IdentityLayout
      avatar={
        <ProjectHoverCard
          handle={project.handle}
          name={name}
          iconUrl={project.iconUrl}
          size="sm"
        />
      }
      name={<Link href={ROUTES.PROJECT(project.handle)}>{name}</Link>}
      handle={project.handle}
      createdAt={createdAt}
      isPreview={isPreview}
      createdViaClientName={createdViaClientName}
    />
  );
}

function AuthorIdentity({
  author,
  createdAt,
  isPreview,
  createdViaClientName,
}: {
  author: PostAuthorSummary;
  createdAt: string;
  isPreview: boolean;
  createdViaClientName?: string | null;
}) {
  return (
    <IdentityLayout
      avatar={
        <ProfileHoverCard
          handle={author.handle}
          name={author.name}
          imageUrl={author.profileImageUrl ?? undefined}
          size="sm"
        />
      }
      name={author.name}
      handle={author.handle}
      createdAt={createdAt}
      isPreview={isPreview}
      createdViaClientName={createdViaClientName}
    />
  );
}

function IdentityLayout({
  avatar,
  name,
  handle,
  createdAt,
  isPreview,
  createdViaClientName,
}: {
  avatar: React.ReactNode;
  name: React.ReactNode;
  handle: string;
  createdAt: string;
  isPreview: boolean;
  createdViaClientName?: string | null;
}) {
  // 피드 카드는 전체가 클릭 영역이라, 안쪽 링크는 카드 이동을 막아야 한다.
  const stopPropagation = isPreview
    ? (event: React.MouseEvent) => event.stopPropagation()
    : undefined;

  return (
    <div className="flex items-center gap-2">
      <div onClick={stopPropagation}>{avatar}</div>

      {/* 피드에서는 이름과 시간만 한 줄로 보여준다. 핸들까지 붙이면 메타
          정보가 제목보다 무거워진다. */}
      {isPreview ? (
        <div className="text-muted-foreground flex items-center gap-1 text-xs">
          <span
            className="text-foreground font-medium"
            onClick={stopPropagation}
          >
            {name}
          </span>
          · <RelativeTime date={createdAt} />
          <DraftedViaBadge clientName={createdViaClientName} />
        </div>
      ) : (
        <div className="flex flex-col">
          <span className="text-sm font-medium">{name}</span>
          <span className="text-muted-foreground flex items-center gap-1 text-xs">
            @{handle} · <RelativeTime date={createdAt} />
            <DraftedViaBadge clientName={createdViaClientName} />
          </span>
        </div>
      )}
    </div>
  );
}
