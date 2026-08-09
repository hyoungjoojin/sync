import type { GetPostResponseContentMediaItem } from '@/api/__generated__/types';

import type { PostContent } from '../types';

/**
 * 뷰어가 다루는 두 가지 본문 표현(문자열, 객체)을 하나로 맞춘다.
 *
 * `json` 이 비어 있을 수 있다 — 에이전트가 만들어 아직 변환되지 않은 Markdown 초안이 그렇다.
 * 뷰어는 Tiptap JSON 만 그리므로 그 경우 본문 없음으로 취급하고, 호출부가 빈 문서를 보여준다.
 */
export function normalizePostContent(content: PostContent | undefined):
  | {
      json: string;
      media: GetPostResponseContentMediaItem[];
    }
  | undefined {
  if (content === undefined) {
    return undefined;
  }

  if (typeof content === 'string') {
    return { json: content, media: [] };
  }

  if (!content.json) {
    return undefined;
  }

  return { json: content.json, media: content.media };
}
