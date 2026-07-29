'use client';

import { ChatCircleIcon, HeartIcon } from '@phosphor-icons/react';

import { useLikePost } from '@/components/feature/post/hooks/useLikePost';
import { useUnlikePost } from '@/components/feature/post/hooks/useUnlikePost';
import { Button } from '@/components/ui/button';
import { useRequireAuth } from '@/hooks/use-require-auth';
import { cn } from '@/lib/utils';

import type { PostSummary } from '../types';

interface PostCardActionsProps {
  summary: PostSummary;
}

export function PostCardActions({ summary }: PostCardActionsProps) {
  const { id: postId, liked, likeCount, commentCount } = summary;
  const { requireAuth } = useRequireAuth();

  const { mutate: likePost } = useLikePost();
  const { mutate: unlikePost } = useUnlikePost();

  const toggleLike = () => {
    const mutate = liked ? unlikePost : likePost;

    mutate({ postId: String(postId) });
  };

  return (
    <div className="flex items-center gap-1">
      <Button
        variant="ghost"
        size="sm"
        onClick={(event) => {
          event.stopPropagation();

          if (!requireAuth({ intent: 'like' })) {
            return;
          }

          toggleLike();
        }}
      >
        <HeartIcon
          className={cn(liked && 'fill-destructive text-destructive')}
          weight={liked ? 'fill' : 'regular'}
        />
        {likeCount}
      </Button>

      <Button
        variant="ghost"
        size="sm"
        onClick={(event) => {
          if (!requireAuth({ intent: 'comment' })) {
            event.stopPropagation();
          }
        }}
      >
        <ChatCircleIcon />
        {commentCount}
      </Button>
    </div>
  );
}
