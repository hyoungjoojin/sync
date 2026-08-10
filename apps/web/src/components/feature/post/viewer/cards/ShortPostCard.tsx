import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { cn } from '@/lib/utils';

import { PostBody } from '../components/PostBody';
import { PostCardActions } from '../components/PostCardActions';
import { PostCardTitle } from '../components/PostCardTitle';
import { PostTags } from '../components/PostTags';
import { PostViewHeader } from '../components/PostViewHeader';
import {
  POST_DETAIL_HEADER,
  POST_DETAIL_PADDING_X,
  POST_DETAIL_SURFACE,
  type PostDetailCardProps,
} from './types';

export function ShortPostCard({
  summary,
  editor,
  postPath,
  lockedPreview,
}: PostDetailCardProps) {
  return (
    <Card className={POST_DETAIL_SURFACE}>
      <CardHeader className={cn(POST_DETAIL_PADDING_X, POST_DETAIL_HEADER)}>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="detail"
        />
      </CardHeader>

      <CardContent className={cn(POST_DETAIL_PADDING_X, 'space-y-3')}>
        <PostCardTitle title={summary.title} variant="detail" />
        <PostBody editor={editor} lockedPreview={lockedPreview} />
        <PostCardActions summary={summary} variant="detail" />
        <PostTags tags={summary.tags} />
      </CardContent>
    </Card>
  );
}
