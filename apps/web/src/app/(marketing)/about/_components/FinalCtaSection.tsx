import { getTranslations } from 'next-intl/server';

import { LinkButton } from '@/components/ui/button';
import ROUTES from '@/util/routes';

/** 5. 최종 CTA — 베타 참여 안내 카드. */
export default async function FinalCtaSection() {
  const t = await getTranslations('pages.about.finalCta');

  return (
    <section className="mx-auto w-full max-w-6xl px-6 py-24">
      <div className="relative overflow-hidden rounded-2xl border border-border bg-card px-8 py-16 text-center">
        <div
          className="pointer-events-none absolute inset-0 -z-10 opacity-[0.05]"
          style={{
            backgroundImage:
              'radial-gradient(circle at 50% 0%, var(--color-brand), transparent 60%)',
          }}
        />
        <h2 className="mx-auto max-w-xl text-3xl font-medium tracking-tight md:text-4xl">
          {t('title')}
        </h2>
        <p className="mx-auto mt-4 max-w-md text-muted-foreground">
          {t('description')}
        </p>

        <p className="mx-auto mt-4 max-w-md text-xs text-muted-foreground">
          {t('note')}
        </p>

        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <LinkButton size="lg" href={ROUTES.REGISTER()}>
            {t('action')}
          </LinkButton>
        </div>
      </div>
    </section>
  );
}
