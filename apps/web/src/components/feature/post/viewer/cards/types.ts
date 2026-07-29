import type { Editor } from '@tiptap/react';

import type { PostSummary } from '../types';

/**
 * 작성자 줄과 본문 사이의 간격을 없앤다. `Card`의 기본 간격은 `gap-6`과
 * `data-[size=sm]:gap-4` 두 개라, `gap-0`만 넘기면 tailwind-merge가 앞의 것만
 * 지우고 `data-[size=sm]:gap-4`가 그대로 살아남는다(속성 선택자라 우선순위도
 * 더 높다). 두 개를 같이 덮어써야 실제로 없어진다.
 */
const POST_CARD_TIGHT_ROWS = 'gap-0 data-[size=sm]:gap-0';

/**
 * 게시물 카드는 테두리와 그림자 없이 표면 색 대비만으로 구분한다. 카드 안에
 * 이미지·태그·액션이 겹겹이 들어가는데 바깥 테두리까지 있으면 경계선이 중첩돼
 * 지저분해진다.
 */
export const POST_CARD_SURFACE = `border-0 shadow-none ${POST_CARD_TIGHT_ROWS}`;

/**
 * 피드 카드가 놓이는 자리에 따른 겉모습.
 *
 * - `card`: 관련 게시물처럼 낱개로 떠 있는 자리. 카드 표면색으로 구분한다.
 * - `flat`: 목록처럼 위아래로 이어지는 자리. 카드 표면을 없애고 목록 쪽에서
 *   그은 가는 선과 hover 색으로만 구분한다.
 */
export type PostPreviewSurface = 'card' | 'flat';

export const POST_PREVIEW_SURFACE: Record<PostPreviewSurface, string> = {
  card: POST_CARD_SURFACE,
  flat: `hover:bg-muted/40 rounded-none border-0 bg-transparent shadow-none transition-colors ${POST_CARD_TIGHT_ROWS}`,
};

/** 상세 화면 카드(`PostCard`)가 각 타입별 카드에 넘기는 공통 입력. */
export interface PostDetailCardProps {
  summary: PostSummary;
  editor: Editor | null;
  postPath: string;
  /** 유료 게이트로 본문이 빠진 응답에서 대체로 보여줄 미리보기 텍스트. */
  lockedPreview?: string;
}

/** 피드 카드(`PostPreviewCard`)가 각 타입별 카드에 넘기는 공통 입력. */
export interface PostPreviewCardTypeProps {
  summary: PostSummary;
  postPath: string;
  onClick: () => void;
  fillHeight?: boolean;
  surface: PostPreviewSurface;
}
