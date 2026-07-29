'use client';

import { PostType } from '../../types/post';
import type { PostCardVariant, PostSummary } from '../types';
import { PostActionsMenu } from './PostActionsMenu';
import { PostHeaderIdentity } from './PostHeaderIdentity';
import { QuestionStatusMarker } from './QuestionStatusMarker';

export function PostViewHeader({
  summary,
  postPath,
  variant,
}: {
  summary: PostSummary;
  postPath: string;
  variant: PostCardVariant;
}) {
  const isPreview = variant === 'preview';

  return (
    <div className="flex items-start justify-between">
      <PostHeaderIdentity summary={summary} isPreview={isPreview} />

      <div className="flex shrink-0 items-center">
        {summary.type === PostType.QUESTION && (
          // 피드 카드는 전체가 클릭 영역이라, 아이콘을 눌러 툴팁을 볼 때
          // 게시물로 넘어가지 않게 막는다.
          <div
            onClick={
              isPreview
                ? (event: React.MouseEvent) => event.stopPropagation()
                : undefined
            }
          >
            <QuestionStatusMarker resolved={summary.resolved} />
          </div>
        )}

        <PostActionsMenu
          summary={summary}
          postPath={postPath}
          variant={variant}
        />
      </div>
    </div>
  );
}
