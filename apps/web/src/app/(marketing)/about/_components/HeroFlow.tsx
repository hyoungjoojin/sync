import { getTranslations } from 'next-intl/server';
import Image from 'next/image';
import type { CSSProperties } from 'react';

const MASK_LAYERS = [
  'linear-gradient(215deg, #000 17%, rgba(0, 0, 0, 0.6) 50%, transparent 76%)',
  'linear-gradient(to right, transparent 0%, #000 30%)',
  'linear-gradient(to top, transparent 0%, #000 27%)',
].join(', ');

const FLOW_SIZES = '(min-width: 1024px) 79vw, 96vw';

const FLOW_SRC = '/assets/hero/hero-flow.webp';

const DIAGONAL_MASK: CSSProperties = {
  maskImage: MASK_LAYERS,
  WebkitMaskImage: MASK_LAYERS,
  maskComposite: 'intersect',
  WebkitMaskComposite: 'source-in',
};

/** 히어로 배경의 생성형 플로우 — 우상단 절반만 채우고 대각선으로 사라진다. */
export default async function HeroFlow() {
  const t = await getTranslations('pages.about.hero');

  return (
    <div
      aria-hidden
      className="pointer-events-none absolute inset-0 overflow-hidden"
    >
      <div
        className="absolute -right-[6%] -top-[20%] h-[105%] w-[96%] opacity-90 lg:w-[79%]"
        style={DIAGONAL_MASK}
      >
        <Image
          src={FLOW_SRC}
          alt={t('art.alt')}
          fill
          priority
          sizes={FLOW_SIZES}
          className="object-cover"
        />
      </div>
    </div>
  );
}
