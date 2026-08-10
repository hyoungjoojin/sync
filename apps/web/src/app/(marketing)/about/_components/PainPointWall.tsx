'use client';

import { useEffect, useRef, useState } from 'react';

import { cn } from '@/lib/utils';

type Tier = 1 | 2 | 3;

/**
 * 배치 순서와 tier는 프레젠테이션 정보라 컴포넌트가 소유한다. tier 1은
 * 인덱스 1·5·9·13·17에 균등 간격으로 배치되고, 같은 알파벳(=같은 주제)이
 * 연속으로 오지 않는다.
 */
const COMPLAINT_ORDER: ReadonlyArray<{ id: string; tier: Tier }> = [
  { id: 'c2', tier: 3 },
  { id: 'a1', tier: 1 },
  { id: 'd2', tier: 3 },
  { id: 'b4', tier: 2 },
  { id: 'e2', tier: 3 },
  { id: 'c1', tier: 1 },
  { id: 'a3', tier: 2 },
  { id: 'b2', tier: 3 },
  { id: 'd3', tier: 2 },
  { id: 'e1', tier: 1 },
  { id: 'c4', tier: 3 },
  { id: 'a4', tier: 3 },
  { id: 'b3', tier: 3 },
  { id: 'd1', tier: 1 },
  { id: 'e3', tier: 2 },
  { id: 'c3', tier: 2 },
  { id: 'a2', tier: 2 },
  { id: 'b1', tier: 1 },
  { id: 'e4', tier: 3 },
  { id: 'd4', tier: 2 },
];

const TIER_TEXT_CLASS: Record<Tier, string> = {
  1: 'text-[21px] font-medium leading-[1.5] text-foreground',
  2: 'text-base font-normal leading-[1.6] text-muted-foreground',
  3: 'hidden text-sm font-normal leading-[1.6] text-muted-foreground/55 md:block',
};

const TIER_START_DELAY: Record<Tier, number> = { 1: 0, 2: 600, 3: 1100 };
const TIER_STEP_DELAY: Record<Tier, number> = { 1: 120, 2: 60, 3: 40 };

export function PainPointWall({
  complaints,
}: {
  complaints: Record<string, string>;
}) {
  const containerRef = useRef<HTMLUListElement>(null);
  const [reducedMotion] = useState(
    () =>
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  );
  const [visible, setVisible] = useState(reducedMotion);
  const animate = !reducedMotion;

  useEffect(() => {
    if (reducedMotion) {
      return;
    }

    const node = containerRef.current;
    if (!node) {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry?.isIntersecting) {
          return;
        }
        setVisible(true);
        observer.disconnect();
      },
      { threshold: 0.15 },
    );

    observer.observe(node);
    return () => observer.disconnect();
  }, [reducedMotion]);

  const tierIndex: Record<Tier, number> = { 1: 0, 2: 0, 3: 0 };

  return (
    <ul
      ref={containerRef}
      className="mx-auto flex max-w-[1040px] flex-col items-start gap-[14px] px-6 md:flex-row md:flex-wrap md:items-baseline md:justify-center md:gap-x-11 md:gap-y-[22px]"
    >
      {COMPLAINT_ORDER.map(({ id, tier }) => {
        const index = tierIndex[tier]++;
        const delay = TIER_START_DELAY[tier] + index * TIER_STEP_DELAY[tier];

        return (
          <li
            key={id}
            className={cn(
              'whitespace-nowrap',
              TIER_TEXT_CLASS[tier],
              animate &&
                'transition-[opacity,transform] duration-[400ms] ease-out',
              animate &&
                (visible
                  ? 'translate-y-0 opacity-100'
                  : 'translate-y-[6px] opacity-0'),
            )}
            style={animate ? { transitionDelay: `${delay}ms` } : undefined}
          >
            {complaints[id]}
          </li>
        );
      })}
    </ul>
  );
}
