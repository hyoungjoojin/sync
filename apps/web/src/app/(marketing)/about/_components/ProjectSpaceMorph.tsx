'use client';

import { useEffect, useState } from 'react';

import { cn } from '@/lib/utils';

import { useReveal } from './Reveal';

const DWELL_MS = 3400;

const EXIT_MS = 420;

const ENTER_MS = 600;

const STAGGER_MS = 60;

const HANDOVER_MS = 40;

const CLEAR_MARGIN = 20;

const ENTER_EASE = 'cubic-bezier(0.22, 1, 0.36, 1)';

const EXIT_EASE = 'cubic-bezier(0.55, 0, 1, 0.45)';

interface Block {
  x: number;
  y: number;
  w: number;
  h: number;
  from: -1 | 1;
  accent?: boolean;
}

const LAYOUTS: Block[][] = [
  [
    { x: 4, y: 9, w: 30, h: 15, from: -1, accent: true },
    { x: 4, y: 31, w: 92, h: 17, from: 1 },
    { x: 4, y: 55, w: 92, h: 17, from: -1 },
    { x: 4, y: 79, w: 92, h: 14, from: 1 },
  ],
  [
    { x: 4, y: 9, w: 29.33, h: 39, from: -1 },
    { x: 35.33, y: 9, w: 29.33, h: 39, from: 1, accent: true },
    { x: 66.67, y: 9, w: 29.33, h: 39, from: -1 },
    { x: 4, y: 52, w: 29.33, h: 39, from: 1 },
    { x: 35.33, y: 52, w: 29.33, h: 39, from: -1 },
    { x: 66.67, y: 52, w: 29.33, h: 39, from: 1 },
  ],
  [
    { x: 4, y: 9, w: 20, h: 84, from: -1, accent: true },
    { x: 28, y: 9, w: 68, h: 38, from: 1 },
    { x: 28, y: 53, w: 33, h: 40, from: -1 },
    { x: 63, y: 53, w: 33, h: 40, from: 1 },
  ],
];

const LONGEST_LAYOUT = Math.max(...LAYOUTS.map((blocks) => blocks.length));

/** 나가는 블록이 카드를 완전히 비운 뒤에야 다음 블록이 들어오도록 미룬다. */
const ENTER_DELAY_MS =
  EXIT_MS + (LONGEST_LAYOUT - 1) * STAGGER_MS + HANDOVER_MS;

function exitOffset({ x, w, from }: Block) {
  return from < 0
    ? -((x + w) / w) * 100 - CLEAR_MARGIN
    : ((100 - x) / w) * 100 + CLEAR_MARGIN;
}

/** 카드 4 — 블록이 좌우로 드나들며 같은 공간이 다른 모양으로 다시 짜인다. */
export default function ProjectSpaceMorph() {
  const { playing, reducedMotion } = useReveal();
  const [step, setStep] = useState(0);

  useEffect(() => {
    if (!playing || reducedMotion) {
      return;
    }

    const cycle = window.setInterval(
      () => setStep((current) => (current + 1) % LAYOUTS.length),
      DWELL_MS,
    );

    return () => window.clearInterval(cycle);
  }, [playing, reducedMotion]);

  const settledStep = reducedMotion ? 0 : step;

  return (
    <div aria-hidden="true" className="mt-4 min-h-40 w-full flex-1 lg:min-h-0">
      <div className="relative h-full w-full overflow-hidden rounded-xl border border-border bg-background">
        {LAYOUTS.map((blocks, layout) =>
          blocks.map((block, index) => {
            const isActive = layout === settledStep;

            return (
              <div
                key={`${layout}-${index}`}
                className={cn(
                  'absolute rounded-md will-change-transform',
                  block.accent === true ? 'bg-primary/25' : 'bg-muted',
                )}
                style={{
                  left: `${block.x}%`,
                  top: `${block.y}%`,
                  width: `${block.w}%`,
                  height: `${block.h}%`,
                  transform: isActive
                    ? 'translateX(0)'
                    : `translateX(${exitOffset(block)}%)`,
                  transition: reducedMotion
                    ? undefined
                    : isActive
                      ? `transform ${ENTER_MS}ms ${ENTER_EASE} ${ENTER_DELAY_MS + index * STAGGER_MS}ms`
                      : `transform ${EXIT_MS}ms ${EXIT_EASE} ${index * STAGGER_MS}ms`,
                }}
              />
            );
          }),
        )}
      </div>
    </div>
  );
}
