'use client';

import {
  BookmarkSimpleIcon,
  DotsThreeIcon,
  LinkSimpleIcon,
  PencilSimpleIcon,
  SirenIcon,
  StackSimpleIcon,
  TrashIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';

import { AddToCollectionDialog } from '@/components/feature/collection/AddToCollectionDialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useRequireAuth } from '@/hooks/use-require-auth';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

import { useBookmarkToggle } from '../hooks/useBookmarkToggle';
import { useDeletePostDialog } from '../hooks/useDeletePostDialog';
import { useReportPostDialog } from '../hooks/useReportPostDialog';
import type { PostCardVariant, PostSummary } from '../types';
import { ReportPostDialog } from './ReportPostDialog';

export function PostActionsMenu({
  summary,
  postPath,
  variant,
}: {
  summary: PostSummary;
  postPath: string;
  variant: PostCardVariant;
}) {
  const t = useTranslations('pages.posts.report');
  const tDelete = useTranslations('pages.posts.delete');
  const tCopyLink = useTranslations('pages.posts.copy-link');
  const tViewer = useTranslations('components.post.viewer');
  const tEdit = useTranslations('pages.posts.edit');
  const tBookmark = useTranslations('pages.posts.bookmark');
  const tCollection = useTranslations('pages.collections');
  const router = useRouter();
  const { requireAuth } = useRequireAuth();
  const [addToCollectionOpen, setAddToCollectionOpen] = useState(false);

  const isPreview = variant === 'preview';
  const report = useReportPostDialog();
  const deleteDialog = useDeletePostDialog(summary.id, {
    redirectTo: isPreview ? undefined : ROUTES.HOME(),
  });
  const { bookmarked } = summary;
  const toggleBookmark = useBookmarkToggle(summary.id, bookmarked);

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(window.location.origin + postPath);
      toast.success(tCopyLink('messages.success'));
    } catch {
      toast.error(tCopyLink('messages.error'));
    }
  };

  const stopPropagation = isPreview
    ? (event: React.MouseEvent) => event.stopPropagation()
    : undefined;

  return (
    <div className="shrink-0">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label={tViewer('options')}
            onClick={stopPropagation}
          >
            <DotsThreeIcon weight="bold" />
          </Button>
        </DropdownMenuTrigger>

        <DropdownMenuContent align="end" onClick={stopPropagation}>
          <DropdownMenuItem
            onSelect={() => {
              if (!requireAuth({ intent: 'bookmark' })) {
                return;
              }
              toggleBookmark();
            }}
          >
            <BookmarkSimpleIcon
              className={cn(bookmarked && 'fill-primary text-primary')}
              weight={bookmarked ? 'fill' : 'regular'}
            />
            {bookmarked ? tBookmark('remove') : tBookmark('add')}
          </DropdownMenuItem>
          <DropdownMenuItem onSelect={handleCopyLink}>
            <LinkSimpleIcon />
            {tCopyLink('trigger')}
          </DropdownMenuItem>
          <DropdownMenuItem
            onSelect={() => {
              if (!requireAuth({ intent: 'collection' })) {
                return;
              }
              setAddToCollectionOpen(true);
            }}
          >
            <StackSimpleIcon />
            {tCollection('add-to-collection.trigger')}
          </DropdownMenuItem>
          {summary.isAuthor && (
            <DropdownMenuItem
              onSelect={() =>
                router.push(
                  summary.project?.handle
                    ? ROUTES.PROJECT_POST_EDIT(
                        summary.project.handle,
                        summary.slug,
                      )
                    : ROUTES.POST_EDIT(summary.slug),
                )
              }
            >
              <PencilSimpleIcon />
              {tEdit('trigger')}
            </DropdownMenuItem>
          )}
          {summary.canDelete && (
            <DropdownMenuItem
              variant="destructive"
              onSelect={() => deleteDialog.open()}
            >
              <TrashIcon />
              {tDelete('trigger')}
            </DropdownMenuItem>
          )}
          {!summary.isAuthor &&
            (isPreview ? (
              <DropdownMenuItem variant="destructive">
                <SirenIcon />
                {t('trigger')}
              </DropdownMenuItem>
            ) : (
              <DropdownMenuItem
                variant="destructive"
                onSelect={() => report.open()}
              >
                <SirenIcon />
                {t('trigger')}
              </DropdownMenuItem>
            ))}
        </DropdownMenuContent>
      </DropdownMenu>

      <div onClick={stopPropagation}>
        <AddToCollectionDialog
          open={addToCollectionOpen}
          onOpenChange={setAddToCollectionOpen}
          postHandle={summary.slug}
          projectHandle={summary.project?.handle}
        />
      </div>

      {!isPreview && (
        <div onClick={stopPropagation}>
          <ReportPostDialog
            postId={summary.id}
            open={report.isOpen}
            onOpenChange={(open) => (open ? report.open() : report.close())}
          />
        </div>
      )}

      <AlertDialog
        open={deleteDialog.isOpen}
        onOpenChange={(open) =>
          open ? deleteDialog.open() : deleteDialog.close()
        }
      >
        <AlertDialogContent onClick={stopPropagation}>
          <AlertDialogHeader>
            <AlertDialogTitle>{tDelete('title')}</AlertDialogTitle>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>{tDelete('actions.cancel')}</AlertDialogCancel>
            <AlertDialogAction
              variant="destructive"
              disabled={deleteDialog.isPending}
              onClick={deleteDialog.confirmDelete}
            >
              {tDelete('actions.confirm')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
