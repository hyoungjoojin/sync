'use client';

import { useTranslations } from 'next-intl';
import { usePathname } from 'next/navigation';

import {
  BRAND_ART,
  TwoColumnFullPageLayout,
} from '@/components/layout/TwoColumnLayout';

interface AuthLayoutProps {
  children?: React.ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  const t = useTranslations('pages.auth.brand');

  const pathname = usePathname();
  const artSrc = pathname?.includes('/register')
    ? BRAND_ART.register
    : BRAND_ART.login;

  return (
    <TwoColumnFullPageLayout
      brandTitle={t('title')}
      brandDescription={t('description')}
      artSrc={artSrc}
    >
      {children}
    </TwoColumnFullPageLayout>
  );
}
