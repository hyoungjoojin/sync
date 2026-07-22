import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

import { requireOnboardedSession } from '@/lib/auth/guards';

import ProfilePosts from '../../profile/[handle]/_components/ProfilePosts';

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('components.layout.sidebar.nav');

  return { title: t('my-posts') };
}

export default async function MyPostsPage() {
  const t = await getTranslations('components.layout.sidebar.nav');
  const session = await requireOnboardedSession();

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-5 px-4 py-8">
      <h1 className="text-xl font-semibold">{t('my-posts')}</h1>

      <ProfilePosts handle={session.user.handle} />
    </main>
  );
}
