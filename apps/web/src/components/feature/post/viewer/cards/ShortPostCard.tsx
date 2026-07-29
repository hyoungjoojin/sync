import { Card, CardContent, CardHeader } from '@/components/ui/card';

import { PostBody } from '../components/PostBody';
import { PostCardActions } from '../components/PostCardActions';
import { PostCardTitle } from '../components/PostCardTitle';
import { PostTags } from '../components/PostTags';
import { PostViewHeader } from '../components/PostViewHeader';
import { POST_CARD_SURFACE, type PostDetailCardProps } from './types';

export function ShortPostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: PostDetailCardProps) {
  return (
    <Card className={POST_CARD_SURFACE}>
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className="space-y-3">
        <PostCardTitle title={summary.title} variant="detail" />
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions summary={summary} />
        <PostTags tags={summary.tags} />
      </CardContent>
    </Card>
  );
}
