'use client';

import { SealCheckIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import { cn } from '@/lib/utils';

/**
 * 질문의 해결 여부를 아이콘 하나로만 보여준다. 채우기와 테두리로 상태를
 * 구분하고, 뜻은 툴팁으로 넘긴다. 헤더에서 옵션 버튼 옆에 놓이는 자리라
 * 배지처럼 글자를 달면 작성자 줄보다 무거워진다.
 */
export function QuestionStatusMarker({ resolved }: { resolved: boolean }) {
  const t = useTranslations('components.post.viewer.question');

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span
          className={cn(
            'flex size-8 shrink-0 items-center justify-center',
            resolved ? 'text-primary' : 'text-muted-foreground',
          )}
          aria-label={resolved ? t('resolved') : t('awaiting')}
        >
          <SealCheckIcon
            className="size-4"
            weight={resolved ? 'fill' : 'regular'}
          />
        </span>
      </TooltipTrigger>

      <TooltipContent>
        {resolved ? t('resolved-hint') : t('awaiting-hint')}
      </TooltipContent>
    </Tooltip>
  );
}
