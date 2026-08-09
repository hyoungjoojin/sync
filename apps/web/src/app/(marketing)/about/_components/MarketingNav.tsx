'use client';

import { useTranslations } from 'next-intl';
import * as React from 'react';

import { LinkButton } from '@/components/ui/button';
import { Logo } from '@/components/ui/logo';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

/**
 * 랜딩 상단 내비게이션.
 *
 * 전체 화면 다크 히어로 위에 겹쳐지므로 히어로가 보이는 동안은 배경 없이 다크
 * 모드로 두고, 히어로를 완전히 지나야 밝은 바로 전환된다. 스크롤량(예: 8px)이
 * 아니라 히어로 노출 여부로 판단해야 히어로가 화면 대부분을 채운 상태에서
 * 너무 일찍 밝은 배경이 겹쳐 보이는 것을 막을 수 있다.
 */
export default function MarketingNav() {
  const t = useTranslations('pages.about.nav');
  const [isScrolled, setIsScrolled] = React.useState(false);

  React.useEffect(() => {
    const hero = document.getElementById('landing-hero');
    if (!hero) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry) setIsScrolled(!entry.isIntersecting);
      },
      { threshold: 0 },
    );
    observer.observe(hero);

    return () => observer.disconnect();
  }, []);

  return (
    <header
      className={cn(
        'fixed inset-x-0 top-0 z-50 border-b transition-colors duration-300',
        isScrolled
          ? 'light border-border/70 bg-background/80 text-foreground backdrop-blur'
          : 'dark border-transparent bg-transparent text-foreground',
      )}
    >
      <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-4">
        <Logo />
        <div className="flex items-center gap-2">
          <LinkButton
            variant="ghost"
            href={ROUTES.LOGIN()}
            className={cn(
              !isScrolled &&
                'text-foreground/80 hover:bg-foreground/10 hover:text-foreground',
            )}
          >
            {t('login')}
          </LinkButton>
          <LinkButton
            href={ROUTES.REGISTER()}
            className={cn(
              !isScrolled &&
                'bg-foreground text-background hover:bg-foreground/85',
            )}
          >
            {t('register')}
          </LinkButton>
        </div>
      </div>
    </header>
  );
}
