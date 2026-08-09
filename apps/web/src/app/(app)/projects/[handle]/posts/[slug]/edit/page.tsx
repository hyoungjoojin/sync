'use client';

import { useQueryClient } from '@tanstack/react-query';
import { useTranslations } from 'next-intl';
import { useParams, useRouter } from 'next/navigation';
import { toast } from 'sonner';

import {
  useGetPostBySlug,
  useUpdateProjectPost,
} from '@/api/__generated__/post/post';
import PostEditor from '@/components/feature/post/editor/PostEditor';
import { invalidatePostQueries } from '@/components/feature/post/hooks/postQueryKeys';
import { useApplySeriesSelection } from '@/components/feature/post/hooks/useApplySeriesSelection';
import { usePostSeriesMembership } from '@/components/feature/post/hooks/usePostSeriesMembership';
import { PostStatus } from '@/components/feature/post/types/post';
import { toPostSummary } from '@/components/feature/post/viewer/types';
import { Skeleton } from '@/components/ui/skeleton';
import { useAuthGuard } from '@/hooks/use-auth-guard';
import SyncError, { ErrorCode } from '@/lib/error';
import ROUTES from '@/util/routes';

export default function EditProjectPostPage() {
  const { handle, slug } = useParams<{ handle: string; slug: string }>();
  const router = useRouter();
  const t = useTranslations('components.editor');
  const queryClient = useQueryClient();
  useAuthGuard();

  const applySeries = useApplySeriesSelection();
  const { data, isPending, isError } = useGetPostBySlug(slug);
  const {
    initialSeries,
    initialSeriesMembership,
    isPending: isSeriesPending,
  } = usePostSeriesMembership(slug);
  const { mutate: updateProjectPost, isPending: isUpdating } =
    useUpdateProjectPost();

  const post = data?.data;

  // 에디터는 시리즈 초기값을 마운트 시점에 한 번만 읽으므로 시리즈 조회까지 기다린다.
  if (isPending || isSeriesPending) {
    return <Skeleton className="h-96 w-full" />;
  }

  if (isError || !post) {
    return (
      <div className="mx-auto flex min-h-96 max-w-2xl items-center justify-center px-6 text-sm text-muted-foreground">
        {t('messages.edit-unavailable')}
      </div>
    );
  }

  const summary = toPostSummary(post.summary);
  const content = post.content;

  if (!summary.isAuthor) {
    return (
      <div className="mx-auto flex min-h-96 max-w-2xl items-center justify-center px-6 text-sm text-muted-foreground">
        {t('messages.edit-forbidden')}
      </div>
    );
  }

  const detailPath = summary.project?.handle
    ? ROUTES.PROJECT_POST(summary.project.handle, summary.slug)
    : ROUTES.POST(summary.slug);
  const editorProject =
    summary.project?.handle && summary.project.name
      ? { handle: summary.project.handle, name: summary.project.name }
      : undefined;

  return (
    <PostEditor
      key={summary.id}
      isEditing
      type={summary.type}
      routeProjectHandle={handle}
      summary={summary}
      content={content}
      initialSeries={initialSeries}
      isSubmitting={isUpdating}
      project={editorProject}
      onSubmit={({
        title,
        type,
        status,
        tags,
        projectTags,
        series,
        coverMediaId,
        removeCover,
        content,
      }) => {
        updateProjectPost(
          {
            handle,
            postId: `${summary.id}`,
            data: {
              title,
              type,
              status,
              tags,
              projectTags,
              coverMediaId,
              removeCover,
              content: {
                json: content.json,
                text: content.text,
                mediaIds: content.media.map((media) => media.id),
              },
            },
          },
          {
            onSuccess: async () => {
              await applySeries({
                slug: summary.slug,
                projectHandle: handle,
                selection: series,
                initial: initialSeriesMembership,
              });
              invalidatePostQueries(queryClient);
              toast.success(t('messages.update-success'));
              if (status === PostStatus.PUBLISHED) {
                router.push(detailPath);
                router.refresh();
              }
            },
            onError: (error) => {
              if (error instanceof SyncError) {
                switch (error.code) {
                  case ErrorCode.POST_NOT_FOUND:
                    toast.error(t('messages.update-error-post-not-found'));
                    return;
                  case ErrorCode.MEDIA_NOT_FOUND:
                    toast.error(t('messages.update-error-media-not-found'));
                    return;
                  case ErrorCode.MEDIA_NOT_UPLOADED:
                    toast.error(t('messages.update-error-media-not-uploaded'));
                    return;
                  case ErrorCode.MEDIA_TOO_LARGE:
                    toast.error(t('messages.update-error-media-too-large'));
                    return;
                  case ErrorCode.UNSUPPORTED_MEDIA_TYPE:
                    toast.error(
                      t('messages.update-error-unsupported-media-type'),
                    );
                    return;
                  case ErrorCode.TAG_LIMIT_EXCEEDED:
                    toast.error(t('messages.update-error-tag-limit-exceeded'));
                    return;
                }
              }
              toast.error(t('messages.update-error'));
            },
          },
        );
      }}
    />
  );
}
