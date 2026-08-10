import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface AdminUsersLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.admin.users');

  return { title: t('title') };
}

export default function AdminUsersLayout({ children }: AdminUsersLayoutProps) {
  return children;
}
