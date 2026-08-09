import { Card, CardContent, CardHeader } from '@/components/ui/card';

import { PostBody } from '../components/PostBody';
import { PostCardActions } from '../components/PostCardActions';
import { PostCardTitle } from '../components/PostCardTitle';
import { PostTags } from '../components/PostTags';
import { PostViewHeader } from '../components/PostViewHeader';
import {
  POST_CARD_SURFACE,
  POST_DETAIL_HEADER,
  type PostDetailCardProps,
} from './types';

export function QuestionPostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: PostDetailCardProps) {
  return (
    <Card className={POST_CARD_SURFACE}>
      <CardHeader className={POST_DETAIL_HEADER}>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-6">
        <PostCardTitle title={summary.title} variant="detail" isQuestion />
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions summary={summary} variant="detail" />
        <PostTags tags={summary.tags} />
      </CardContent>
    </Card>
  );
}
