import { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';

interface AdminTagsLayoutProps {
  children: React.ReactNode;
}

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations('pages.admin.tags');

  return { title: t('title') };
}

export default function AdminTagsLayout({ children }: AdminTagsLayoutProps) {
  return children;
}
