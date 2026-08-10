import { getTranslations } from 'next-intl/server';

import { PainPointWall } from './PainPointWall';

const DOT_GRID = {
  backgroundImage: 'radial-gradient(currentColor 1px, transparent 1px)',
  backgroundSize: '32px 32px',
};

/** 1. 문제 — 흩어진 불만과, 그것이 구조에서 나온다는 이야기. */
export default async function ProblemSolutionSection() {
  const t = await getTranslations('pages.about.problemSolution');
  const rawComplaints = t.raw as (key: string) => Record<string, string>;
  const complaints = rawComplaints('before.complaints');

  return (
    <section className="relative overflow-hidden border-t border-border">
      <div
        className="pointer-events-none absolute inset-0 -z-10 opacity-[0.04]"
        style={DOT_GRID}
      />

      <div className="mx-auto w-full max-w-6xl px-6 pb-6 pt-24">
        <div className="mx-auto max-w-3xl text-center">
          <h2 className="text-4xl font-medium leading-[1.12] tracking-tight md:text-5xl">
            {t('before.heading')}
          </h2>
          <p className="mt-4 text-base leading-relaxed text-muted-foreground">
            {t('before.sub')}
          </p>
        </div>

        <div className="mt-20">
          <PainPointWall complaints={complaints} />
        </div>
      </div>
    </section>
  );
}
