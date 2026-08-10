'use client';

import { useTranslations } from 'next-intl';

import { useGetUserCollections } from '@/api/__generated__/collection/collection';
import { useGetProfileByHandle } from '@/api/__generated__/profile/profile';
import { CollectionGrid } from '@/components/feature/collection/CollectionGrid';
import { useSession } from '@/lib/auth/client';

interface ProfileCollectionsProps {
  handle: string;
}

export default function ProfileCollections({
  handle,
}: ProfileCollectionsProps) {
  const t = useTranslations('pages.collections');
  const { data: session } = useSession();
  const { data: profile } = useGetProfileByHandle(handle);

  const userId = profile?.data.userId;
  const isOwnProfile = !!session?.user.id && String(session.user.id) === userId;

  const { data, isPending } = useGetUserCollections(userId ?? '', undefined, {
    query: { enabled: !!userId },
  });

  const collections = data?.data.collections ?? [];

  return (
    <CollectionGrid
      title={t('title')}
      description={t('description')}
      collections={collections}
      isPending={isPending}
      canCreate={isOwnProfile}
    />
  );
}
