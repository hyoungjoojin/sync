import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { cn } from '@/lib/utils';

import { PostBody } from '../components/PostBody';
import { PostCardActions } from '../components/PostCardActions';
import { PostCardTitle } from '../components/PostCardTitle';
import { PostCoverImage } from '../components/PostCoverImage';
import { PostTags } from '../components/PostTags';
import { PostViewHeader } from '../components/PostViewHeader';
import {
  POST_CARD_SURFACE,
  POST_DETAIL_HEADER,
  type PostDetailCardProps,
} from './types';

export function LongPostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: PostDetailCardProps) {
  // 커버가 늘 맨 위에 붙으므로 카드 위 여백을 없앤다. `Card` 의
  // `has-[>img:first-child]` 규칙은 생성 커버(div)에는 걸리지 않는다.
  return (
    <Card className={cn(POST_CARD_SURFACE, 'pt-0')}>
      <PostCoverImage url={summary.coverImageUrl} seed={String(summary.id)} />

      {/* 카드가 작성자 줄과 본문을 붙여 놓으므로, 커버 바로 아래에 올 때만
          위 여백을 되살린다. */}
      <CardHeader className={cn('pt-6', POST_DETAIL_HEADER)}>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-6">
        <PostCardTitle title={summary.title} variant="detail" />
        {/* 상세 화면이므로 본문을 자르지 않는다. 잘린 미리보기는 피드 카드
            (`LongPostPreviewCard`)의 역할이다. */}
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions summary={summary} variant="detail" />
        <PostTags tags={summary.tags} />
      </CardContent>
    </Card>
  );
}
