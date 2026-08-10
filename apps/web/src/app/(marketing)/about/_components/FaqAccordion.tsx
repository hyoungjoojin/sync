'use client';

import { useEffect, useRef, useState } from 'react';

import { cn } from '@/lib/utils';

const STAGGER_MS = 70;

const ENTER_TRANSITION =
  'transition-[opacity,transform] duration-[380ms] ease-[cubic-bezier(0.16,1,0.3,1)]';

export interface FaqItem {
  id: string;
  question: string;
  answer: string;
}

export default function FaqAccordion({
  heading,
  items,
}: {
  heading: string;
  items: FaqItem[];
}) {
  const [openId, setOpenId] = useState<string | null>(items[0]?.id ?? null);
  const [entered, setEntered] = useState(false);
  const [reducedMotion, setReducedMotion] = useState(false);

  const rootRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = () => setReducedMotion(media.matches);

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
      ([entry]) => {
        if (!entry?.isIntersecting) {
          return;
        }
        setEntered(true);
        observer.disconnect();
      },
      { threshold: 0.2 },
    );

    observer.observe(root);
    return () => observer.disconnect();
  }, []);

  const animate = !reducedMotion;
  const revealed = reducedMotion || entered;

  return (
    <div ref={rootRef} className="mx-auto w-full max-w-[760px] px-6">
      <h2
        className={cn(
          'text-left text-3xl font-semibold',
          animate && ENTER_TRANSITION,
          animate &&
            (revealed
              ? 'translate-y-0 opacity-100'
              : 'translate-y-[10px] opacity-0'),
        )}
      >
        {heading}
      </h2>

      <div className="mt-10 divide-y divide-border border-y border-border">
        {items.map((item, index) => (
          <FaqAccordionItem
            key={item.id}
            item={item}
            open={openId === item.id}
            onToggle={() =>
              setOpenId((current) => (current === item.id ? null : item.id))
            }
            animate={animate}
            revealed={revealed}
            delayMs={(index + 1) * STAGGER_MS}
          />
        ))}
      </div>
    </div>
  );
}

function FaqAccordionItem({
  item,
  open,
  onToggle,
  animate,
  revealed,
  delayMs,
}: {
  item: FaqItem;
  open: boolean;
  onToggle: () => void;
  animate: boolean;
  revealed: boolean;
  delayMs: number;
}) {
  const triggerId = `faq-trigger-${item.id}`;
  const panelId = `faq-panel-${item.id}`;

  return (
    <div
      className={cn(
        animate && ENTER_TRANSITION,
        animate &&
          (revealed
            ? 'translate-y-0 opacity-100'
            : 'translate-y-[10px] opacity-0'),
      )}
      style={animate ? { transitionDelay: `${delayMs}ms` } : undefined}
    >
      <h3>
        <button
          id={triggerId}
          type="button"
          aria-expanded={open}
          aria-controls={panelId}
          onClick={onToggle}
          className="flex w-full cursor-pointer items-center justify-between gap-4 py-5 text-left text-base font-medium"
        >
          {item.question}
          <svg
            viewBox="0 0 24 24"
            aria-hidden="true"
            className={cn(
              'size-4 shrink-0 text-muted-foreground transition-transform duration-200 motion-reduce:transition-none',
              open && 'rotate-180',
            )}
            fill="none"
          >
            <path
              d="m6 9 6 6 6-6"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      </h3>

      <div
        id={panelId}
        role="region"
        aria-labelledby={triggerId}
        className="grid"
        style={{
          gridTemplateRows: open ? '1fr' : '0fr',
          opacity: open ? 1 : 0,
          visibility: open ? 'visible' : 'hidden',
          transition: animate
            ? `grid-template-rows 260ms ease-out, opacity 260ms ease-out, visibility 0s linear ${open ? '0ms' : '260ms'}`
            : 'none',
        }}
      >
        <div className="overflow-hidden">
          <p className="pb-6 pr-12 text-sm leading-relaxed text-muted-foreground">
            {item.answer}
          </p>
        </div>
      </div>
    </div>
  );
}
