'use client';

import { useCallback, useSyncExternalStore } from 'react';

import { getMeshCoverDataUrl } from '@/components/feature/post/editor/cover/generators';
import { cn } from '@/lib/utils';

const subscribe = () => () => {};

/**
 * 커버를 고르지 않은 글에 편집기와 같은 mesh 생성기로 그때그때 커버를 만들어
 * 붙인다. 게시물 ID를 씨앗으로 삼으므로 같은 글은 언제 봐도 같은 그림이다.
 *
 * 캔버스는 브라우저에만 있으니 서버 렌더에서는 빈 바탕만 내보내고, 수화 이후
 * 실제 그림으로 바뀐다.
 */
export function GeneratedPostCover({
  seed,
  className,
}: {
  seed: string;
  className?: string;
}) {
  const dataUrl = useSyncExternalStore(
    subscribe,
    useCallback(() => getMeshCoverDataUrl(seed), [seed]),
    () => null,
  );

  return (
    <div
      className={cn('bg-muted bg-cover bg-center', className)}
      style={dataUrl ? { backgroundImage: `url(${dataUrl})` } : undefined}
    />
  );
}
