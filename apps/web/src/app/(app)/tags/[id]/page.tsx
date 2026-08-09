import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

import { getTag } from '@/api/__generated__/tag/tag';
import TagDetailHeader from '@/components/feature/tag/detail/TagDetailHeader';
import TagPostFeed from '@/components/feature/tag/detail/TagPostFeed';
import { NON_INDEXABLE_METADATA, createTagMetadata } from '@/lib/seo';

interface TagDetailPageProps {
  params: Promise<{
    id: string;
  }>;
}

export async function generateMetadata({
  params,
}: TagDetailPageProps): Promise<Metadata> {
  const { id } = await params;
  const t = await getTranslations('metadata');

  try {
    const { data } = await getTag(id);

    if (!data.tag) {
      return NON_INDEXABLE_METADATA;
    }

    return createTagMetadata(
      data.tag,
      t('tagDescription', { name: data.tag.name }),
    );
  } catch {
    return NON_INDEXABLE_METADATA;
  }
}

export default async function TagDetailPage({ params }: TagDetailPageProps) {
  const { id } = await params;

  return (
    <div className="space-y-6">
      <TagDetailHeader tagId={id} />
      <TagPostFeed tagId={id} />
    </div>
  );
}
