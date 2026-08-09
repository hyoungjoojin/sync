'use client';

import { useTranslations } from 'next-intl';
import { useParams } from 'next/navigation';

import { useGetProjectCollections } from '@/api/__generated__/collection/collection';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import { GetProjectResponseRole } from '@/api/__generated__/types';
import { CollectionGrid } from '@/components/feature/collection/CollectionGrid';

export default function ProjectCollections() {
  const t = useTranslations('pages.collections');
  const { handle } = useParams<{ handle: string }>();

  const { data: project } = useGetProjectByHandle(handle);
  const isAdmin = project?.data.role === GetProjectResponseRole.Admin;

  const { data, isPending } = useGetProjectCollections(handle);
  const collections = data?.data.collections ?? [];

  return (
    <CollectionGrid
      title={t('title')}
      description={t('description')}
      collections={collections}
      isPending={isPending}
      canCreate={isAdmin}
      projectHandle={handle}
    />
  );
}
