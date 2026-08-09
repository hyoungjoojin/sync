import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface PromotionsLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.admin.promotions');

  return { title: t('title') };
}

export default function PromotionsLayout({ children }: PromotionsLayoutProps) {
  return children;
}
