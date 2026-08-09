import { cn } from '@/lib/utils';

import type { PostPreviewMedia } from '../types';

const MAX_VISIBLE = 3;

export function PostPreviewMediaGrid({
  media,
  mediaCount,
}: {
  media: PostPreviewMedia[];
  mediaCount: number;
}) {
  const visible = media.slice(0, MAX_VISIBLE);

  if (visible.length === 0) {
    return null;
  }

  const hiddenCount = mediaCount - visible.length;
  const isSingle = visible.length === 1;
  // 3장 이상이면 왼쪽에 큰 이미지 한 장, 오른쪽에 나머지 두 장을 쌓는다.
  const isMosaic = visible.length >= 3;

  return (
    <div
      className={cn(
        'grid gap-2',
        isSingle && 'grid-cols-1',
        !isSingle && !isMosaic && 'h-64 grid-cols-2',
        isMosaic && 'h-80 grid-cols-2 grid-rows-2',
      )}
    >
      {visible.map((item, index) => (
        <div
          key={item.id}
          className={cn(
            'relative overflow-hidden rounded-xl bg-neutral-900',
            isMosaic && index === 0 && 'row-span-2',
          )}
        >
          {/* 레터박스로 남는 여백을 같은 이미지의 흐린 사본으로 채운다.
              어두운 바탕 위에 옅게 얹어 색감만 배어나오게 한다. */}
          <div
            aria-hidden
            className="absolute inset-0 scale-125 bg-cover bg-center opacity-25 blur-2xl saturate-150"
            style={{ backgroundImage: `url(${item.url})` }}
          />

          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={item.url}
            alt=""
            className={cn(
              'relative w-full object-contain',
              // 한 장이면 원본 비율 그대로 두고 높이만 제한한다. 여러 장이면
              // 격자 높이에 맞춰야 하므로 셀을 가득 채운다.
              isSingle ? 'max-h-[32rem]' : 'h-full',
            )}
            loading="lazy"
          />

          {hiddenCount > 0 && index === visible.length - 1 && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/50 text-lg font-semibold text-white">
              {`+${hiddenCount}`}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
