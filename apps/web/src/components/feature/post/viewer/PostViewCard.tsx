'use client';

import { DotsThreeIcon, SirenIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';

import { useDeletePost } from '@/api/__generated__/post/post';
import type { GetPostResponse } from '@/api/__generated__/types';
import { ProfileHoverCard } from '@/components/feature/profile/ProfileHoverCard';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { RelativeTime } from '@/components/ui/relative-time';
import ROUTES from '@/util/routes';

import {
  PostScope,
  PostStatus,
  PostType,
  isPublicPublishedPost,
} from '../types/post';
import PostErrorBoundary from './PostErrorBoundary';
import { PostCardActions } from './components/PostCardActions';
import { PostTypeBadge } from './components/PostTypeBadge';
import { ReportPostDialog } from './components/ReportPostDialog';
import { useReadOnlyPostEditor } from './hooks/useReadOnlyPostEditor';
import type { PostAuthorSummary, PostProjectSummary } from './types';
import {
  PostBody,
  type PostCardVariant,
  getPostCardStyles,
} from './variants/PostBody';

export interface PostViewCardProps {
  id: number;
  slug: string;
  type?: PostType;
  scope?: PostScope;
  status?: PostStatus;
  title?: string | null;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  content: GetPostResponse['content'];
  likeCount: number;
  commentCount: number;
  bookmarked: boolean;
  isAuthor: boolean;
  createdAt: string;
  variant: PostCardVariant;
}

export default function PostViewCard(props: PostViewCardProps) {
  return (
    <PostErrorBoundary>
      <PostViewCardContent {...props} />
    </PostErrorBoundary>
  );
}

function PostViewCardContent({
  id,
  slug,
  type,
  scope,
  status,
  title,
  author,
  project,
  content,
  likeCount,
  commentCount,
  bookmarked,
  isAuthor,
  createdAt,
  variant,
}: PostViewCardProps) {
  const router = useRouter();
  const editor = useReadOnlyPostEditor(content);
  const { contentClassName, bodyClassName } = getPostCardStyles(variant, type);
  const showActions = isPublicPublishedPost(scope, status);

  const postPath = project?.handle
    ? ROUTES.PROJECT_POST(project.handle, slug)
    : ROUTES.POST(slug);

  const handleClickCard =
    variant === 'preview' ? () => router.push(postPath) : undefined;

  return (
    <Card onClick={handleClickCard}>
      <CardHeader>
        <PostViewCardHeader
          postId={id}
          postPath={postPath}
          type={type}
          scope={scope}
          status={status}
          author={author}
          project={project}
          isAuthor={isAuthor}
          createdAt={createdAt}
          variant={variant}
        />
      </CardHeader>

      <CardContent className={contentClassName}>
        {title && <h3 className="text-lg font-semibold">{title}</h3>}
        <PostBody type={type} editor={editor} className={bodyClassName} />
        {showActions && (
          <PostCardActions
            postId={id}
            likeCount={likeCount}
            commentCount={commentCount}
            bookmarked={bookmarked}
          />
        )}
      </CardContent>
    </Card>
  );
}

function PostViewCardHeader({
  postId,
  postPath,
  type,
  scope,
  status,
  author,
  project,
  isAuthor,
  createdAt,
  variant,
}: {
  postId: number;
  postPath: string;
  type?: PostType;
  scope?: PostScope;
  status?: PostStatus;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  isAuthor: boolean;
  createdAt: string;
  variant: PostCardVariant;
}) {
  const t = useTranslations('pages.posts.report');
  const tPost = useTranslations('components.post');
  const tDelete = useTranslations('pages.posts.delete');
  const tCopyLink = useTranslations('pages.posts.copy-link');
  const router = useRouter();
  const queryClient = useQueryClient();
  const [reportOpen, setReportOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const { mutate: deletePost, isPending: isDeleting } = useDeletePost();

  const isPreview = variant === 'preview';

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(window.location.origin + postPath);
      toast.success(tCopyLink('messages.success'));
    } catch {
      toast.error(tCopyLink('messages.error'));
    }
  };

  const handleDelete = () => {
    deletePost(
      { postId: String(postId) },
      {
        onSuccess: () => {
          toast.success(tDelete('messages.success'));
          setDeleteOpen(false);

          if (isPreview) {
            queryClient.invalidateQueries({
              predicate: (query) =>
                typeof query.queryKey[1] === 'string' &&
                query.queryKey[1].startsWith('/posts'),
            });
          } else {
            router.push(ROUTES.HOME());
          }
        },
        onError: () => {
          toast.error(tDelete('messages.error'));
        },
      },
    );
  };

  const stopPropagation = isPreview
    ? (event: React.MouseEvent) => event.stopPropagation()
    : undefined;

  return (
    <>
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2">
          <ProfileHoverCard
            handle={author.handle}
            name={author.name}
            size={isPreview ? 'default' : 'sm'}
          />

          <div className="flex flex-col">
            <span className="text-sm font-semibold">{author.name}</span>
            <span className="text-muted-foreground text-xs">
              @{author.handle} · <RelativeTime date={createdAt} />
            </span>
          </div>

          {type && <PostTypeBadge type={type} />}

          {status === PostStatus.DRAFT && (
            <Badge variant="outline">{tPost('status.DRAFT')}</Badge>
          )}

          {scope === PostScope.WORKSPACE && (
            <Badge variant="outline">{tPost('scope.WORKSPACE')}</Badge>
          )}

          {project?.name && <Badge variant="secondary">{project.name}</Badge>}
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="Post options"
              onClick={stopPropagation}
            >
              <DotsThreeIcon weight="bold" />
            </Button>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end" onClick={stopPropagation}>
            <DropdownMenuItem onSelect={handleCopyLink}>
              {tCopyLink('trigger')}
            </DropdownMenuItem>
            {isAuthor ? (
              <DropdownMenuItem
                variant="destructive"
                onSelect={() => setDeleteOpen(true)}
              >
                {tDelete('trigger')}
              </DropdownMenuItem>
            ) : isPreview ? (
              <DropdownMenuItem variant="destructive">Report</DropdownMenuItem>
            ) : (
              <DropdownMenuItem
                variant="destructive"
                onSelect={() => setReportOpen(true)}
              >
                <SirenIcon />
                {t('trigger')}
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {!isPreview && (
        <ReportPostDialog
          postId={postId}
          open={reportOpen}
          onOpenChange={setReportOpen}
        />
      )}

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent onClick={stopPropagation}>
          <AlertDialogHeader>
            <AlertDialogTitle>{tDelete('title')}</AlertDialogTitle>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>{tDelete('actions.cancel')}</AlertDialogCancel>
            <AlertDialogAction
              variant="destructive"
              disabled={isDeleting}
              onClick={handleDelete}
            >
              {tDelete('actions.confirm')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
