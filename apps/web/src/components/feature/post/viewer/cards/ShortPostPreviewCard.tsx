import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { cn } from '@/lib/utils';

import { PostCardActions } from '../components/PostCardActions';
import { PostPreviewBody } from '../components/PostPreviewBody';
import { PostPreviewMediaGrid } from '../components/PostPreviewMediaGrid';
import { PostViewHeader } from '../components/PostViewHeader';
import { POST_PREVIEW_SURFACE, type PostPreviewCardTypeProps } from './types';

export function ShortPostPreviewCard({
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
      <CardHeader>
        <PostViewHeader
          summary={summary}
          postPath={postPath}
          variant="preview"
        />
      </CardHeader>

      <CardContent className="space-y-3">
        <PostPreviewBody
          preview={summary.preview}
          className="text-foreground"
        />

        <PostPreviewMediaGrid
          media={summary.previewMedia}
          mediaCount={summary.mediaCount}
        />

        <PostCardActions summary={summary} />
      </CardContent>
    </Card>
  );
}
