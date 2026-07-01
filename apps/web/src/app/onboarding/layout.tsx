'use client';

import { useTranslations } from 'next-intl';

import { TwoColumnFullPageLayout } from '@/components/layout/TwoColumnLayout';

interface OnboardingLayoutProps {
  children?: React.ReactNode;
}

export default function OnboardingLayout({ children }: OnboardingLayoutProps) {
  const t = useTranslations('pages.onboarding.brand');

  return (
    <TwoColumnFullPageLayout
      brandTitle={t('title')}
      brandDescription={t('description')}
    >
      {children}
    </TwoColumnFullPageLayout>
  );
}
