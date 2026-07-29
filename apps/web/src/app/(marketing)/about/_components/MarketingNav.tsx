'use client';

import { useTranslations } from 'next-intl';
import { useEffect, useState } from 'react';

import { LinkButton } from '@/components/ui/button';
import { Logo } from '@/components/ui/logo';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

/**
 * 랜딩 상단 내비게이션.
 *
 * 전체 화면 다크 히어로 위에 겹쳐지므로 최상단에서는 배경 없이 다크 모드로 두고,
 * 스크롤이 시작되면 밝은 바로 전환된다.
 */
export default function MarketingNav() {
  const t = useTranslations('pages.about.nav');
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const onScroll = (): void => {
      setIsScrolled(window.scrollY > 8);
    };

    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });

    return () => {
      window.removeEventListener('scroll', onScroll);
    };
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
