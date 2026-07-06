'use client';

import { DotsThreeIcon, SirenIcon } from '@phosphor-icons/react';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
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

import { ImageNode } from '../editor/extensions/nodes/image';
import { deserialize } from '../editor/utils/serializer';
import { PostType } from '../types/post';
import PostErrorBoundary from './PostErrorBoundary';
import { PostCardActions } from './components/PostCardActions';
import { PostTypeBadge } from './components/PostTypeBadge';
import { ReportPostDialog } from './components/ReportPostDialog';
import type { PostAuthorSummary, PostProjectSummary } from './types';
import { PostBody } from './variants/PostBody';

interface PostCardProps {
  id: number;
  type: PostType;
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

export default function PostCard(props: PostCardProps) {
  return (
    <PostErrorBoundary>
      <PostCardContent {...props} />
    </PostErrorBoundary>
  );
}

function PostCardContent({
  id,
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
}: PostCardProps) {
  const editor = useEditor({
    extensions: [StarterKit, ImageNode],
    content: deserialize(content.json, content.media),
    editable: false,
    immediatelyRender: false,
  });

  return (
    <Card>
      <CardHeader>
        <PostCardHeader
          postId={id}
          type={type}
          author={author}
          project={project}
          isAuthor={isAuthor}
          createdAt={createdAt}
        />
      </CardHeader>

      <CardContent
        className={type === PostType.SHORT ? 'space-y-3' : 'space-y-4'}
      >
        {title && <h3 className="text-lg font-semibold">{title}</h3>}
        <PostBody
          type={type}
          editor={editor}
          className={type === PostType.LONG ? 'line-clamp-4' : undefined}
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

function PostCardHeader({
  postId,
  type,
  author,
  project,
  isAuthor,
  createdAt,
}: {
  postId: number;
  type: PostType;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  isAuthor: boolean;
  createdAt: string;
}) {
  const t = useTranslations('pages.posts.report');
  const tDelete = useTranslations('pages.posts.delete');
  const router = useRouter();
  const [reportOpen, setReportOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const { mutate: deletePost, isPending: isDeleting } = useDeletePost();

  const handleDelete = () => {
    deletePost(
      { postId: String(postId) },
      {
        onSuccess: () => {
          toast.success(tDelete('messages.success'));
          setDeleteOpen(false);
          router.push(ROUTES.HOME());
        },
        onError: () => {
          toast.error(tDelete('messages.error'));
        },
      },
    );
  };

  return (
    <>
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2">
          <ProfileHoverCard
            handle={author.handle}
            name={author.name}
            size="sm"
          />

          <div className="flex flex-col">
            <span className="text-sm font-semibold">{author.name}</span>
            <span className="text-muted-foreground text-xs">
              @{author.handle} · <RelativeTime date={createdAt} />
            </span>
          </div>

          <PostTypeBadge type={type} />

          {project?.name && <Badge variant="secondary">{project.name}</Badge>}
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon-sm" aria-label="Post options">
              <DotsThreeIcon weight="bold" />
            </Button>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end">
            <DropdownMenuItem>Copy link</DropdownMenuItem>
            {isAuthor ? (
              <DropdownMenuItem
                variant="destructive"
                onSelect={() => {
                  setDeleteOpen(true);
                }}
              >
                {tDelete('trigger')}
              </DropdownMenuItem>
            ) : (
              <DropdownMenuItem
                variant="destructive"
                onSelect={() => {
                  setReportOpen(true);
                }}
              >
                <SirenIcon />
                {t('trigger')}
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <ReportPostDialog
        postId={postId}
        open={reportOpen}
        onOpenChange={setReportOpen}
      />

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
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
