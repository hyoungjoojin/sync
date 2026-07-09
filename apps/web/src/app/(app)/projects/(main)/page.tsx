import { EnvelopeSimpleIcon, PlusIcon } from '@phosphor-icons/react/dist/ssr';
import Link from 'next/link';

import { LinkButton } from '@/components/ui/button';
import { requireSession } from '@/lib/auth/guards';
import ROUTES from '@/util/routes';

import FollowingProjects from './_components/FollowingProjects';
import UserProjects from './_components/UserProjects';

export default async function Projects() {
  await requireSession();

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold">프로젝트</h1>
          <p className="text-muted-foreground text-sm">
            내가 속해 있거나, 팔로우 중이거나, 초대받은 스페이스
          </p>
        </div>

        <div className="flex items-center gap-2">
          <LinkButton href={ROUTES.PROJECT_INVITATIONS()} variant="outline">
            <EnvelopeSimpleIcon />
            초대장
          </LinkButton>

          <LinkButton href={ROUTES.NEW_PROJECT()}>
            <PlusIcon />새 프로젝트
          </LinkButton>
        </div>
      </div>

      <section className="space-y-3">
        <h2 className="text-muted-foreground text-xs font-semibold tracking-wide uppercase">
          내 프로젝트
        </h2>

        <UserProjects />
      </section>

      <section className="space-y-3">
        <div className="flex items-center justify-between gap-3 rounded-lg p-4">
          <h2 className="text-muted-foreground text-xs font-semibold tracking-wide uppercase">
            팔로우 중
          </h2>

          <Link
            href={ROUTES.EXPLORE_PROJECTS()}
            className="text-primary inline-block text-sm font-medium hover:underline"
          >
            탐색 페이지에서 더 많은 프로젝트 찾아보기 →
          </Link>
        </div>

        <FollowingProjects />
      </section>
    </div>
  );
}
