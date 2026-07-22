import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

import ProjectInvitations from './_components/ProjectInvitations';

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.projects.invitations');

  return { title: t('heading') };
}

export default function ProjectInvitationsPage() {
  return <ProjectInvitations />;
}
