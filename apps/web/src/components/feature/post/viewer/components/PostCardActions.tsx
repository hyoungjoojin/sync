'use client';

import { ChatCircleIcon, HeartIcon } from '@phosphor-icons/react';

import { useLikePost } from '@/components/feature/post/hooks/useLikePost';
import { useUnlikePost } from '@/components/feature/post/hooks/useUnlikePost';
import { Button } from '@/components/ui/button';
import { useRequireAuth } from '@/hooks/use-require-auth';
import { cn } from '@/lib/utils';

import type { PostCardVariant, PostSummary } from '../types';
import { focusCommentComposer } from '../utils/commentComposer';

interface PostCardActionsProps {
  summary: PostSummary;
  variant?: PostCardVariant;
}

export function PostCardActions({
  summary,
  variant = 'preview',
}: PostCardActionsProps) {
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
            return;
          }

          // 피드 카드에서는 이벤트를 그대로 흘려보내 카드 클릭(게시물 이동)에
          // 맡기고, 상세 화면에서는 같은 페이지의 댓글 입력창으로 보낸다.
          if (variant === 'detail') {
            event.stopPropagation();
            focusCommentComposer();
          }
        }}
      >
        <ChatCircleIcon />
        {commentCount}
      </Button>
    </div>
  );
}
