import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface ResetPasswordLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.reset_password');

  return { title: t('title') };
}

export default function ResetPasswordLayout({
  children,
}: ResetPasswordLayoutProps) {
  return children;
}
