'use client';

import Image, { type StaticImageData } from 'next/image';
import { useEffect, useRef, useState } from 'react';

const DWELL_MS = 3200;

const PUSH_MS = 620;

const FADE_MS = 1100;

const BOUNCE = 'cubic-bezier(0.34, 1.56, 0.64, 1)';

const EXIT_EASE = 'cubic-bezier(0.5, 0, 1, 1)';

const EXIT_DROP = 115;

const QUEUE_DEPTH = 0.4;

const CARD_HEIGHT = 86;

const CARD_WIDTH = 76;

const CARD_INSET_Y = 2;

const OFFSET_PERCENT = 7;

const SCALE_STEP = 0.045;

const IMAGE_SIZES = '(min-width: 768px) 25vw, 90vw';

const QUEUED = -2;

const EJECTED = -1;

interface CardInstance {
  id: number;
  slide: number;
  slot: number;
}

function settleExit(cards: CardInstance[]) {
  const front = cards.find((card) => card.slot === 0);

  return cards.map((card) =>
    card.slot === EJECTED
      ? { ...card, slot: QUEUED, slide: front?.slide ?? card.slide }
      : card,
  );
}

function advance(cards: CardInstance[], count: number) {
  return settleExit(cards).map((card) => {
    if (card.slot === QUEUED) {
      return { ...card, slot: count - 1 };
    }

    return card.slot >= 0 ? { ...card, slot: card.slot - 1 } : card;
  });
}

function stackCenter(count: number) {
  return (-(count - 1) * OFFSET_PERCENT) / 2;
}

function slotTransform(slot: number, center: number) {
  return `translate(calc(-50% + ${slot * OFFSET_PERCENT + center}%), ${
    slot * -OFFSET_PERCENT
  }%) scale(${1 - slot * SCALE_STEP})`;
}

export interface PostTypeSlide {
  key: string;
  tag: string;
  name: string;
  description: string;
  image: StaticImageData;
  alt: string;
}

export default function PostTypeCarousel({
  slides,
}: {
  slides: PostTypeSlide[];
}) {
  const [instances, setInstances] = useState<CardInstance[]>(() => [
    ...slides.map((_, index) => ({ id: index, slide: index, slot: index })),
    { id: slides.length, slide: 0, slot: QUEUED },
  ]);
  const [isVisible, setIsVisible] = useState(false);
  const [isPageVisible, setIsPageVisible] = useState(true);
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(false);

  const rootRef = useRef<HTMLDivElement>(null);

  const isPlaying = isVisible && isPageVisible && !prefersReducedMotion;

  useEffect(() => {
    const sync = () => setIsPageVisible(document.visibilityState === 'visible');

    sync();
    document.addEventListener('visibilitychange', sync);

    return () => document.removeEventListener('visibilitychange', sync);
  }, []);

  useEffect(() => {
    const query = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = () => setPrefersReducedMotion(query.matches);

    sync();
    query.addEventListener('change', sync);

    return () => query.removeEventListener('change', sync);
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

    const cycle = window.setInterval(
      () => setInstances((current) => advance(current, slides.length)),
      DWELL_MS,
    );

    return () => window.clearInterval(cycle);
  }, [isPlaying, slides.length]);

  const front = instances.find((card) => card.slot === 0);
  const frontSlide = front === undefined ? undefined : slides[front.slide];
  if (frontSlide === undefined) {
    return null;
  }

  return (
    <div
      ref={rootRef}
      className="mt-3 flex min-h-0 flex-1 flex-col gap-2 lg:flex-row lg:gap-4"
    >
      <div className="relative min-h-40 w-full flex-1 overflow-hidden md:min-h-0 lg:w-auto">
        {instances.map((card) => {
          const slide = slides[card.slide];
          if (slide === undefined) {
            return null;
          }

          const isQueued = card.slot === QUEUED;
          const isEjected = card.slot === EJECTED;

          const center = stackCenter(slides.length);

          const transform = isEjected
            ? `translate(calc(-50% + ${center}%), ${EXIT_DROP}%) scale(1)`
            : slotTransform(
                isQueued ? slides.length - 1 + QUEUE_DEPTH : card.slot,
                center,
              );

          return (
            <div
              key={card.id}
              aria-hidden={card.slot !== 0}
              className="absolute overflow-hidden rounded-xl border border-border bg-card shadow-lg will-change-transform"
              style={{
                height: `${CARD_HEIGHT}%`,
                width: `${CARD_WIDTH}%`,
                left: '50%',
                bottom: `${CARD_INSET_Y}%`,
                transform,
                opacity: isQueued ? 0 : 1,
                zIndex: isEjected
                  ? slides.length + 2
                  : slides.length - card.slot,
                transition:
                  isQueued || prefersReducedMotion
                    ? undefined
                    : isEjected
                      ? `transform ${PUSH_MS}ms ${EXIT_EASE}`
                      : `transform ${PUSH_MS}ms ${BOUNCE}, opacity ${FADE_MS}ms linear`,
              }}
            >
              <Image
                src={slide.image}
                alt={slide.alt}
                fill
                sizes={IMAGE_SIZES}
                className="object-cover object-left-top"
              />
            </div>
          );
        })}
      </div>

      <div
        key={frontSlide.key}
        className="shrink-0 animate-in fade-in-0 duration-500 lg:w-40 lg:self-center"
      >
        <p className="text-sm font-medium">{frontSlide.name}</p>
        <p className="mt-0.5 min-h-8 text-xs leading-relaxed text-muted-foreground">
          {frontSlide.description}
        </p>
      </div>
    </div>
  );
}
