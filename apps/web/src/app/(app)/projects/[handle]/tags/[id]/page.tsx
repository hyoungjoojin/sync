import { HydrationBoundary, dehydrate } from '@tanstack/react-query';
import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { notFound } from 'next/navigation';

import { getGetProjectByHandleQueryOptions } from '@/api/__generated__/project/project';
import { getTag } from '@/api/__generated__/tag/tag';
import TagDetailHeader from '@/components/feature/tag/detail/TagDetailHeader';
import TagPostFeed from '@/components/feature/tag/detail/TagPostFeed';
import SyncError, { ErrorCode } from '@/lib/error';
import { getQueryClient } from '@/lib/query';
import { NON_INDEXABLE_METADATA, createTagMetadata } from '@/lib/seo';

interface ProjectTagDetailPageProps {
  params: Promise<{
    handle: string;
    id: string;
  }>;
}

export async function generateMetadata({
  params,
}: ProjectTagDetailPageProps): Promise<Metadata> {
  const { handle, id } = await params;
  const t = await getTranslations('metadata');

  try {
    const { data } = await getTag(id);

    if (!data.tag) {
      return NON_INDEXABLE_METADATA;
    }

    // 프로젝트 태그 화면은 소속 프로젝트 경로가 정규 URL이다.
    return createTagMetadata(
      { ...data.tag, projectHandle: data.tag.projectHandle ?? handle },
      t('tagDescription', { name: data.tag.name }),
    );
  } catch {
    return NON_INDEXABLE_METADATA;
  }
}

export default async function ProjectTagDetailPage({
  params,
}: ProjectTagDetailPageProps) {
  const { handle, id } = await params;

  const queryClient = getQueryClient();

  try {
    await queryClient.fetchQuery(getGetProjectByHandleQueryOptions(handle));
  } catch (error) {
    if (error instanceof SyncError) {
      switch (error.code) {
        case ErrorCode.PROJECT_NOT_FOUND:
          notFound();
      }
    }
  }

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <div className="space-y-6">
        <TagDetailHeader tagId={id} />
        <TagPostFeed tagId={id} />
      </div>
    </HydrationBoundary>
  );
}
