import { getTranslations } from 'next-intl/server';

import { LinkButton } from '@/components/ui/button';
import ROUTES from '@/util/routes';

import HeroFlow from './HeroFlow';
import HeroHeadline from './HeroHeadline';

/** 0. 히어로 — 전체 화면 다크. */
export default async function HeroSection() {
  const t = await getTranslations('pages.about.hero');

  return (
    <section
      id="landing-hero"
      className="dark relative flex min-h-screen w-full flex-col justify-end overflow-hidden bg-background text-foreground"
    >
      <HeroFlow />

      <div className="relative z-10 mx-auto w-full max-w-6xl px-6 pb-[16vh] pt-32">
        <HeroHeadline />

        <div className="mt-10">
          <LinkButton
            size="lg"
            href={ROUTES.REGISTER()}
            className="h-12 rounded-full bg-foreground px-8 text-background hover:bg-foreground/85"
          >
            {t('actions.register')}
          </LinkButton>
        </div>
      </div>
    </section>
  );
}
