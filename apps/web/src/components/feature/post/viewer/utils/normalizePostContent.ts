import type { GetPostResponseContentMediaItem } from '@/api/__generated__/types';

import type { PostContent } from '../types';

export function normalizePostContent(content: PostContent | undefined):
  | {
      json: string;
      media: GetPostResponseContentMediaItem[];
    }
  | undefined {
  if (content === undefined) {
    return undefined;
  }

  return typeof content === 'string' ? { json: content, media: [] } : content;
}
