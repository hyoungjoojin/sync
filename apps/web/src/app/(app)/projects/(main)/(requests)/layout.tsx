import { ArrowLeftIcon } from '@phosphor-icons/react/dist/ssr';
import { getTranslations } from 'next-intl/server';
import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { requireSession } from '@/lib/auth/guards';
import ROUTES from '@/util/routes';

import ProjectRequestsTabs from './_components/ProjectRequestsTabs';

export default async function ProjectRequestsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  await requireSession();

  const t = await getTranslations('pages.projects.requests');

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" asChild>
        <Link href={ROUTES.PROJECTS()}>
          <ArrowLeftIcon className="size-4" />
          {t('back')}
        </Link>
      </Button>

      <ProjectRequestsTabs>{children}</ProjectRequestsTabs>
    </div>
  );
}
