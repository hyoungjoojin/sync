import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils';

import { PostCardActions } from '../components/PostCardActions';
import { PostCardTitle } from '../components/PostCardTitle';
import { PostPreviewBody } from '../components/PostPreviewBody';
import { PostPreviewMediaGrid } from '../components/PostPreviewMediaGrid';
import { PostTags } from '../components/PostTags';
import { PostViewHeader } from '../components/PostViewHeader';
import { POST_PREVIEW_SURFACE, type PostPreviewCardTypeProps } from './types';

export function QuestionPostPreviewCard({
  summary,
  postPath,
  onClick,
  fillHeight,
  surface,
}: PostPreviewCardTypeProps) {
  return (
    <Card
      size="sm"
      onClick={onClick}
      className={cn(POST_PREVIEW_SURFACE[surface], fillHeight && 'h-full')}
    >
      <CardContent className="min-w-0 space-y-3">
        {/* 작성자 줄은 제목 묶음과 한 덩어리로 둔다. 바깥 `space-y-3` 에 걸리면
            작성자와 본문 사이만 벌어진다. */}
        <div>
          <PostViewHeader
            summary={summary}
            postPath={postPath}
            variant="preview"
          />

          <div className="space-y-1">
            <PostCardTitle title={summary.title} variant="preview" isQuestion />
            <PostPreviewBody preview={summary.preview} />
          </div>
        </div>

        <PostPreviewMediaGrid
          media={summary.previewMedia}
          mediaCount={summary.mediaCount}
        />

        <div className="space-y-2">
          <PostTags tags={summary.tags} />
          <PostCardActions summary={summary} />
        </div>
      </CardContent>
    </Card>
  );
}
