import { useTranslations } from 'next-intl';

import { cn } from '@/lib/utils';

const WORDS_PER_MINUTE = 200;

/**
 * 커버 위에 겹쳐 놓는 읽는 시간. 뒤에 오는 것이 테마 색이 아니라 커버 이미지라
 * 밝기를 알 수 없으므로, 테마를 따라가지 않고 흰 알약에 어두운 글씨로 고정한다.
 */
export function ReadingTimeBadge({
  wordCount,
  className,
}: {
  wordCount: number;
  className?: string;
}) {
  const t = useTranslations('components.post.viewer');
  const minutes = Math.max(1, Math.ceil(wordCount / WORDS_PER_MINUTE));

  return (
    <span
      className={cn(
        'rounded-full bg-white/90 px-2 py-0.5 text-[11px] font-medium text-neutral-900 shadow-sm backdrop-blur-sm',
        className,
      )}
    >
      {t('minRead', { minutes })}
    </span>
  );
}
