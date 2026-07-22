import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

import { requireOnboardedSession } from '@/lib/auth/guards';

import CollectionsList from './_components/CollectionsList';

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.collections');

  return { title: t('title') };
}

export default async function CollectionsPage() {
  const session = await requireOnboardedSession();

  return <CollectionsList userId={String(session.user.id)} />;
}
