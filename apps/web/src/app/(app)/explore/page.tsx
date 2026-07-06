import { getTranslations } from 'next-intl/server';

import Posts from '../(home)/_components/Posts';
import RecommendedUsers from './_components/RecommendedUsers';

export default async function ExplorePage() {
  const t = await getTranslations('pages.explore');

  return (
    <div className="mx-auto max-w-2xl space-y-6 px-4 py-8">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold">{t('title')}</h1>
        <p className="text-muted-foreground text-sm">{t('description')}</p>
      </div>

      <RecommendedUsers />

      <Posts />
    </div>
  );
}
