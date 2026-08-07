'use client';

import { ArrowSquareOutIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import { useGetPostBySlug } from '@/api/__generated__/post/post';
import type { GetPostResponse } from '@/api/__generated__/types';
import { PostBody } from '@/components/feature/post/viewer/components/PostBody';
import { useReadOnlyPostEditor } from '@/components/feature/post/viewer/hooks/useReadOnlyPostEditor';
import { LinkButton } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { useModal } from '@/hooks/store';
import ROUTES from '@/util/routes';

interface PromotionPostPreviewProps {
  projectHandle: string;
  postSlug: string;
}

export default function PromotionPostPreview({
  projectHandle,
  postSlug,
}: PromotionPostPreviewProps) {
  const { data, isPending } = useGetPostBySlug(postSlug);

  if (isPending || !data) {
    return <Skeleton className="h-32 w-full" />;
  }

  return (
    <PromotionPostPreviewContent
      projectHandle={projectHandle}
      postSlug={postSlug}
      post={data.data}
    />
  );
}

interface PromotionPostPreviewContentProps {
  projectHandle: string;
  postSlug: string;
  post: GetPostResponse;
}

// Tiptap's useEditor only applies `content` on initial mount, not on later
// re-renders — so this must only ever mount once real post data exists. The
// parent component's isPending/!data early return guarantees that.
function PromotionPostPreviewContent({
  projectHandle,
  postSlug,
  post,
}: PromotionPostPreviewContentProps) {
  const t = useTranslations('modals.promotions');
  const editor = useReadOnlyPostEditor(post.content, postSlug);
  const { closeModal } = useModal();

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-4">
        <h3 className="text-xl font-semibold">{post.summary.title}</h3>
        <LinkButton
          href={ROUTES.PROJECT_POST(projectHandle, postSlug)}
          variant="outline"
          size="icon-sm"
          aria-label={t('actions.go-to-post')}
          onClick={closeModal}
        >
          <ArrowSquareOutIcon />
        </LinkButton>
      </div>
      <PostBody editor={editor} />
    </div>
  );
}
