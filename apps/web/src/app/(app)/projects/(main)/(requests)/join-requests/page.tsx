import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

import MyProjectJoinRequests from './_components/MyProjectJoinRequests';

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.projects.requests.join-requests');

  return { title: t('heading') };
}

export default function ProjectJoinRequestsPage() {
  return <MyProjectJoinRequests />;
}
