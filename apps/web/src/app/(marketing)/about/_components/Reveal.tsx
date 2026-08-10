'use client';

import { createContext, useContext, useEffect, useRef, useState } from 'react';

import { cn } from '@/lib/utils';

const STAGGER_MS = 90;

const ENTER_MS = 420;

const DEMO_DELAY_MS = 200;

interface RevealState {
  /** 데모가 재생 중이어도 되는지 — 시작됐고, 화면에 보이고, 모션이 허용될 때. */
  playing: boolean;
  reducedMotion: boolean;
}

const RevealContext = createContext<RevealState>({
  playing: false,
  reducedMotion: false,
});

/** 카드 안 데모 컴포넌트가 재생 가능 여부를 읽는 훅. */
export function useReveal() {
  return useContext(RevealContext);
}

/**
 * 벤토 카드 하나를 감싸 진입 애니메이션과 카드 안 데모 재생 시점을 조율한다.
 * 화면에 들어오면 index * 90ms 만큼 지연 후 떠오르고, 그 트랜지션이 끝난 뒤
 * 200ms 뒤에 데모가 시작된다. 호버·포커스는 children을 리마운트해 데모를
 * 처음부터 다시 재생한다.
 */
export default function RevealProvider({
  index,
  className,
  children,
}: {
  index: number;
  className?: string;
  children: React.ReactNode;
}) {
  const [entered, setEntered] = useState(false);
  const [visible, setVisible] = useState(false);
  const [started, setStarted] = useState(false);
  const [replayKey, setReplayKey] = useState(0);
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
        const isIntersecting = entry?.isIntersecting ?? false;
        setVisible(isIntersecting);
        if (isIntersecting) {
          setEntered(true);
        }
      },
      { threshold: 0.2 },
    );

    observer.observe(root);

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!entered || reducedMotion) {
      return;
    }

    const delay = index * STAGGER_MS + ENTER_MS + DEMO_DELAY_MS;
    const timeout = window.setTimeout(() => setStarted(true), delay);

    return () => window.clearTimeout(timeout);
  }, [entered, index, reducedMotion]);

  const revealed = reducedMotion || entered;

  const replay = () => {
    if (started || reducedMotion) {
      setReplayKey((key) => key + 1);
    }
  };

  return (
    <div
      ref={rootRef}
      onMouseEnter={replay}
      onFocus={replay}
      className={cn(
        'transition-[opacity,transform] duration-[420ms] ease-[cubic-bezier(0.16,1,0.3,1)] motion-reduce:!transition-none',
        revealed ? 'translate-y-0 opacity-100' : 'translate-y-3 opacity-0',
        className,
      )}
      style={
        reducedMotion
          ? undefined
          : { transitionDelay: `${index * STAGGER_MS}ms` }
      }
    >
      <RevealContext.Provider
        value={{
          playing: (started || reducedMotion) && visible,
          reducedMotion,
        }}
      >
        <div key={replayKey} className="contents">
          {children}
        </div>
      </RevealContext.Provider>
    </div>
  );
}
