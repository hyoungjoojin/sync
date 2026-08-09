import { cn } from '@/lib/utils';

import { QUESTION_TITLE_PREFIX } from '../../constants';
import type { PostCardVariant } from '../types';

const TITLE_SIZE: Record<PostCardVariant, string> = {
  preview: 'text-xl leading-snug',
  detail: 'text-2xl leading-tight',
};

export function PostCardTitle({
  title,
  variant = 'preview',
  isQuestion = false,
  className,
}: {
  title?: string | null;
  variant?: PostCardVariant;
  isQuestion?: boolean;
  className?: string;
}) {
  if (!title) {
    return null;
  }

  if (isQuestion) {
    return (
      <h3
        className={cn(
          'flex gap-2 tracking-tight',
          TITLE_SIZE[variant],
          className,
        )}
      >
        {/* 표식만 굵게 두고 제목은 본문 굵기로 남긴다. 표식과 제목을 각각 다른
            칸에 두어 제목이 여러 줄이 되어도 표식 아래로 파고들지 않는다. */}
        <span className="font-semibold">{QUESTION_TITLE_PREFIX}</span>
        <span className="min-w-0 font-normal text-balance">{title}</span>
      </h3>
    );
  }

  return (
    <h3
      className={cn(
        'font-semibold tracking-tight text-balance',
        TITLE_SIZE[variant],
        className,
      )}
    >
      {title}
    </h3>
  );
}
