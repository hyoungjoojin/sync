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
 * 상세 화면에서 작성자 줄과 본문 사이 여백. `POST_CARD_TIGHT_ROWS`가 지운
 * 간격을 상세 카드에서만 되살리는 값이라, 그 규칙을 바꿀 때 같이 본다.
 * 피드 카드는 붙어 있는 지금 간격이 맞으므로 쓰지 않는다.
 */
export const POST_DETAIL_HEADER = 'pb-4';

/**
 * 상세 화면 카드는 모바일 화면에서 카드 표면(배경·안쪽 여백)을 없애 본문이
 * 페이지에 바로 놓이게 하고, sm 이상에서만 카드로 돌아온다. `Card` 기본값이
 * 이미 `rounded-none sm:rounded-xl`로 모서리를 반응형으로 다루므로 같은
 * 패턴을 배경·상하 여백까지 넓힌 것이다. 세로 방향은 `py`가 아니라
 * `pt`/`pb`로 따로 쓴다 — `py`로 합쳐 쓰면 표지 이미지가 있는
 * `LongPostCard`가 `sm:pt-0`로 위쪽만 다시 없앨 때 tailwind-merge가
 * `sm:py-6` 전체(위+아래)를 지워버려 아래쪽 여백까지 함께 사라진다.
 */
export const POST_DETAIL_SURFACE = `${POST_CARD_SURFACE} bg-transparent pt-0 pb-0 sm:bg-card sm:pt-6 sm:pb-6`;

/** 카드 표면을 없앤 모바일에서 헤더·본문의 좌우 여백도 같이 없앤다. */
export const POST_DETAIL_PADDING_X = 'px-0 sm:px-6';

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
