'use client';

import { DotsThreeIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { useTranslations } from 'next-intl';
import { redirect } from 'next/navigation';
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

import { ImageNode } from '../editor/extensions/nodes/image';
import { deserialize } from '../editor/utils/serializer';
import { PostType } from '../types/post';
import PostErrorBoundary from './PostErrorBoundary';
import { PostCardActions } from './components/PostCardActions';
import { PostTypeBadge } from './components/PostTypeBadge';
import type { PostAuthorSummary, PostProjectSummary } from './types';
import { PostBody } from './variants/PostBody';

interface PostPreviewProps {
  id: number;
  slug: string;
  type?: PostType;
  title?: string | null;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  content: GetPostResponse['content'];
  likeCount: number;
  commentCount: number;
  bookmarked: boolean;
  isAuthor: boolean;
  createdAt: string;
}

export default function PostPreview(props: PostPreviewProps) {
  return (
    <PostErrorBoundary>
      <PostPreviewContent {...props} />
    </PostErrorBoundary>
  );
}

function PostPreviewContent({
  id,
  slug,
  type,
  title,
  author,
  project,
  content,
  likeCount,
  commentCount,
  bookmarked,
  isAuthor,
  createdAt,
}: PostPreviewProps) {
  const editor = useEditor({
    extensions: [StarterKit, ImageNode],
    content: deserialize(content.json, content.media),
    editable: false,
    immediatelyRender: false,
  });

  const handleClickCard = () => {
    if (project?.handle) {
      redirect(ROUTES.PROJECT_POST(project.handle, slug));
    } else {
      redirect(ROUTES.POST(slug));
    }
  };

  return (
    <Card onClick={handleClickCard}>
      <CardHeader>
        <PostPreviewHeader
          postId={id}
          type={type}
          author={author}
          project={project}
          isAuthor={isAuthor}
          createdAt={createdAt}
        />
      </CardHeader>

      <CardContent className="space-y-4">
        {title && <h3 className="text-lg font-semibold">{title}</h3>}
        <PostBody
          type={type}
          editor={editor}
          className={type === PostType.LONG ? 'line-clamp-6' : undefined}
        />
        <PostCardActions
          postId={id}
          likeCount={likeCount}
          commentCount={commentCount}
          bookmarked={bookmarked}
        />
      </CardContent>
    </Card>
  );
}

function PostPreviewHeader({
  postId,
  type,
  author,
  project,
  isAuthor,
  createdAt,
}: {
  postId: number;
  type?: PostType;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  isAuthor: boolean;
  createdAt: string;
}) {
  const tDelete = useTranslations('pages.posts.delete');
  const queryClient = useQueryClient();
  const [deleteOpen, setDeleteOpen] = useState(false);
  const { mutate: deletePost, isPending: isDeleting } = useDeletePost();

  const handleDelete = () => {
    deletePost(
      { postId: String(postId) },
      {
        onSuccess: () => {
          toast.success(tDelete('messages.success'));
          setDeleteOpen(false);
          queryClient.invalidateQueries({
            predicate: (query) =>
              typeof query.queryKey[1] === 'string' &&
              query.queryKey[1].startsWith('/posts'),
          });
        },
        onError: () => {
          toast.error(tDelete('messages.error'));
        },
      },
    );
  };

  return (
    <div className="flex items-start justify-between">
      <div className="flex items-center gap-2">
        <ProfileHoverCard
          handle={author.handle}
          name={author.name}
          size="default"
        />

        <div className="flex flex-col">
          <span className="text-sm font-semibold">{author.name}</span>
          <span className="text-muted-foreground text-xs">
            @{author.handle} · <RelativeTime date={createdAt} />
          </span>
        </div>

        {type && <PostTypeBadge type={type} />}

        {project?.name && <Badge variant="secondary">{project.name}</Badge>}
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label="Post options"
            onClick={(event) => event.stopPropagation()}
          >
            <DotsThreeIcon weight="bold" />
          </Button>
        </DropdownMenuTrigger>

        <DropdownMenuContent
          align="end"
          onClick={(event) => event.stopPropagation()}
        >
          <DropdownMenuItem>Copy link</DropdownMenuItem>
          {isAuthor ? (
            <DropdownMenuItem
              variant="destructive"
              onSelect={() => setDeleteOpen(true)}
            >
              {tDelete('trigger')}
            </DropdownMenuItem>
          ) : (
            <DropdownMenuItem variant="destructive">Report</DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent onClick={(event) => event.stopPropagation()}>
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
    </div>
  );
}
