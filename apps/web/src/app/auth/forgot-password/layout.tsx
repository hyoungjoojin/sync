import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface ForgotPasswordLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.forgot_password');

  return { title: t('title') };
}

export default function ForgotPasswordLayout({
  children,
}: ForgotPasswordLayoutProps) {
  return children;
}
