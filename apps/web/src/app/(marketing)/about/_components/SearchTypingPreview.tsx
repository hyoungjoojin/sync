'use client';

import { useEffect, useRef, useState } from 'react';

import { cn } from '@/lib/utils';

const TYPE_MS = 88;

const SEARCH_MS = 460;

const REVEAL_MS = 200;

const HOLD_MS = 2600;

const FADE_MS = 480;

export interface SearchResult {
  key: string;
  title: string;
}

interface Cursor {
  chars: number;
  revealed: number;
  fading: boolean;
}

const START: Cursor = { chars: 0, revealed: 0, fading: false };

/** 검색 타일 안에서 오타 섞인 검색어가 결과를 찾아내는 과정을 재생한다. */
export default function SearchTypingPreview({
  query,
  results,
}: {
  query: string;
  results: SearchResult[];
}) {
  const [cursor, setCursor] = useState<Cursor>(START);
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

    let delay: number;
    let next: Cursor;

    if (cursor.fading) {
      delay = FADE_MS;
      next = START;
    } else if (cursor.chars < query.length) {
      delay = TYPE_MS;
      next = { ...cursor, chars: cursor.chars + 1 };
    } else if (cursor.revealed < results.length) {
      delay = cursor.revealed === 0 ? SEARCH_MS : REVEAL_MS;
      next = { ...cursor, revealed: cursor.revealed + 1 };
    } else {
      delay = HOLD_MS;
      next = { ...cursor, fading: true };
    }

    const timeout = window.setTimeout(() => setCursor(next), delay);

    return () => window.clearTimeout(timeout);
  }, [cursor, isPlaying, query.length, results.length]);

  const settled = prefersReducedMotion
    ? { chars: query.length, revealed: results.length, fading: false }
    : cursor;

  const isTyping = settled.chars < query.length;

  return (
    <div
      ref={rootRef}
      aria-hidden="true"
      className="mt-3 flex min-h-0 flex-1 flex-col gap-1.5 transition-opacity duration-500"
      style={{ opacity: settled.fading ? 0 : 1 }}
    >
      <div className="flex shrink-0 items-center gap-2 rounded-lg border border-border bg-background px-2.5 py-1.5">
        <svg
          viewBox="0 0 24 24"
          className={cn(
            'size-4 shrink-0 transition-colors duration-300',
            isTyping ? 'text-muted-foreground' : 'text-primary',
          )}
          fill="none"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" />
          <path
            d="m20 20-3-3"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          />
        </svg>
        <span className="truncate text-sm">
          {query.slice(0, settled.chars)}
          {isTyping && (
            <span className="ml-0.5 inline-block h-[1em] w-0.5 translate-y-[0.15em] rounded-full bg-primary motion-safe:animate-pulse" />
          )}
        </span>
      </div>

      <div className="flex flex-col gap-1">
        {results.slice(0, settled.revealed).map((result) => (
          <div
            key={result.key}
            className="flex animate-in items-center gap-2 fade-in-0 slide-in-from-bottom-1 rounded-lg border border-border bg-background/60 px-2.5 py-1 duration-300"
          >
            <span className="size-1 shrink-0 rounded-full bg-primary/60" />
            <span className="truncate text-xs text-muted-foreground">
              {result.title}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
