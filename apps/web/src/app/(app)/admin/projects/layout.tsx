import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface AdminProjectsLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.admin.projects');

  return { title: t('title') };
}

export default function AdminProjectsLayout({
  children,
}: AdminProjectsLayoutProps) {
  return children;
}
