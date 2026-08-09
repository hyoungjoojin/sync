'use client';

import { useEffect, useRef, useState } from 'react';

const CARD_ANGLES = [-28, -14, 0, 14, 28];

const CENTER = (CARD_ANGLES.length - 1) / 2;

const OPEN_MS = 2600;

const CLOSED_MS = 1100;

const SPREAD_MS = 90;

/** 컬렉션 타일 안에서 카드 묶음이 손에 쥔 패처럼 펼쳐졌다 다시 모인다. */
export default function CollectionDeck() {
  const [isOpen, setIsOpen] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(false);

  const rootRef = useRef<HTMLDivElement>(null);

  const isPlaying = isVisible && !prefersReducedMotion;

  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = () => setPrefersReducedMotion(media.matches);

    sync();
    media.addEventListener('change', sync);

    return () => media.removeEventListener('change', sync);
  }, []);

  useEffect(() => {
    const root = rootRef.current;
    if (root === null) {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => setIsVisible(entry?.isIntersecting ?? false),
      { threshold: 0.3 },
    );

    observer.observe(root);

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!isPlaying) {
      return;
    }

    const timeout = window.setTimeout(
      () => setIsOpen((open) => !open),
      isOpen ? OPEN_MS : CLOSED_MS,
    );

    return () => window.clearTimeout(timeout);
  }, [isOpen, isPlaying]);

  const settledOpen = prefersReducedMotion ? true : isOpen;

  return (
    <div
      ref={rootRef}
      aria-hidden="true"
      className="pointer-events-none absolute inset-0 -z-10"
    >
      {CARD_ANGLES.map((angle, index) => {
        const distance = Math.abs(index - CENTER);
        const transform = settledOpen
          ? `translateX(-50%) rotate(${angle}deg)`
          : `translateX(-50%) rotate(0deg) translateY(${distance * 3}px) scale(${1 - distance * 0.03})`;

        return (
          <div
            key={angle}
            className="absolute -bottom-14 left-1/2 h-28 w-[40%] min-w-28 rounded-lg border border-border bg-background shadow-sm shadow-foreground/5 transition-transform duration-700 ease-out dark:shadow-black/40"
            style={{
              transform,
              transformOrigin: '50% 190%',
              transitionDelay: `${(settledOpen ? distance : CENTER - distance) * SPREAD_MS}ms`,
              zIndex: CARD_ANGLES.length - distance,
              opacity: 1 - distance * 0.12,
            }}
          >
            <div className="flex flex-col gap-1.5 p-2.5">
              <span
                className="h-1.5 w-6 rounded-full bg-primary"
                style={{ opacity: 1 - distance * 0.3 }}
              />
              <span className="h-1 w-full rounded-full bg-foreground/12" />
              <span className="h-1 w-2/3 rounded-full bg-foreground/12" />
            </div>
          </div>
        );
      })}
    </div>
  );
}
