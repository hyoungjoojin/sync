'use client';

import { usePathname } from 'next/navigation';

// `/projects/new` 는 프로젝트 생성 화면이라 핸들이 아니다.
const PROJECT_CONTEXT_PATTERN = /^\/projects\/(?!new(?:\/|$))([^/]+)/;

/**
 * 지금 보고 있는 화면이 어느 프로젝트 안인지 알려준다. 개인 화면이면
 * `undefined`. 사이드바가 개인/프로젝트 모드를 고르는 기준과 같은 규칙이라,
 * 사이드바와 게시물 카드가 항상 같은 맥락을 본다.
 */
export function useProjectContextHandle() {
  const pathname = usePathname();

  return pathname.match(PROJECT_CONTEXT_PATTERN)?.[1];
}
