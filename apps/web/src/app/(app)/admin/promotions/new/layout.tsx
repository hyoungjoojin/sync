import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface NewPromotionLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.admin.promotions');

  return { title: t('new.title') };
}

export default function NewPromotionLayout({
  children,
}: NewPromotionLayoutProps) {
  return children;
}
