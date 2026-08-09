'use client';

import { useTranslations } from 'next-intl';
import { useEffect, useMemo, useState } from 'react';

const TYPE_MS = 110;

const DELETE_MS = 45;

const HOLD_MS = 1900;

const SWAP_MS = 340;

/**
 * 히어로 헤드라인.
 *
 * 앞 단어는 `pages.about.hero.rotatingWords` 배열을 순서대로 타이핑/삭제하며,
 * 단어를 추가하거나 빼려면 ko.json의 그 배열만 수정하면 된다.
 */
export default function HeroHeadline() {
  const t = useTranslations('pages.about.hero');
  const words = useMemo(() => t.raw('rotatingWords') as string[], [t]);

  const [index, setIndex] = useState(0);
  const [length, setLength] = useState(0);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isStatic, setIsStatic] = useState(false);

  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = (): void => {
      setIsStatic(media.matches);
    };

    sync();
    media.addEventListener('change', sync);

    return () => {
      media.removeEventListener('change', sync);
    };
  }, []);

  useEffect(() => {
    if (isStatic || words.length === 0) {
      return;
    }

    const word = words[index] ?? '';
    const isTyped = !isDeleting && length >= word.length;
    const isCleared = isDeleting && length === 0;

    const timer = window.setTimeout(
      () => {
        if (isCleared) {
          setIsDeleting(false);
          setIndex((current) => (current + 1) % words.length);

          return;
        }

        if (isTyped) {
          setIsDeleting(true);

          return;
        }

        setLength((current) => current + (isDeleting ? -1 : 1));
      },
      isCleared
        ? SWAP_MS
        : isTyped
          ? HOLD_MS
          : isDeleting
            ? DELETE_MS
            : TYPE_MS,
    );

    return () => {
      window.clearTimeout(timer);
    };
  }, [index, isDeleting, isStatic, length, words]);

  const typed = isStatic
    ? (words[0] ?? '')
    : (words[index] ?? '').slice(0, length);

  return (
    <h1 className="text-[2.75rem] font-medium leading-[1.08] tracking-[-0.035em] sm:text-6xl lg:text-[4.5rem]">
      <span className="sr-only">
        {(words[0] ?? '') + ' ' + t('titleSuffix')}
      </span>

      <span aria-hidden className="block">
        <span className="block whitespace-nowrap text-brand-accent">
          {typed}
          <span className="ml-1 inline-block h-[0.72em] w-[0.05em] animate-pulse bg-brand-accent align-baseline" />
        </span>

        <span className="block">{t('titleSuffix')}</span>
      </span>
    </h1>
  );
}
