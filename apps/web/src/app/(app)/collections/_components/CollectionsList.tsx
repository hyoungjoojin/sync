'use client';

import { useTranslations } from 'next-intl';

import { useGetUserCollections } from '@/api/__generated__/collection/collection';
import { CollectionGrid } from '@/components/feature/collection/CollectionGrid';

export default function CollectionsList({ userId }: { userId: string }) {
  const t = useTranslations('pages.collections');

  const { data, isPending } = useGetUserCollections(userId);
  const collections = data?.data.collections ?? [];

  return (
    <CollectionGrid
      title={t('title')}
      description={t('description')}
      collections={collections}
      isPending={isPending}
      canCreate
    />
  );
}
