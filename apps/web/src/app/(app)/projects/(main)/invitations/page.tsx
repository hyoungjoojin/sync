import { ArrowLeftIcon } from '@phosphor-icons/react/dist/ssr';
import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { requireSession } from '@/lib/auth/guards';
import ROUTES from '@/util/routes';

import ProjectInvitations from './_components/ProjectInvitations';

export default async function ProjectInvitationsPage() {
  await requireSession();

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" asChild>
        <Link href={ROUTES.PROJECTS()}>
          <ArrowLeftIcon className="size-4" />
          프로젝트 대시보드로 돌아가기
        </Link>
      </Button>

      <ProjectInvitations />
    </div>
  );
}
