'use client';

import { useTranslations } from 'next-intl';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import ROUTES from '@/util/routes';

export default function ProjectRequestsTabs({
  children,
}: {
  children: React.ReactNode;
}) {
  const t = useTranslations('pages.projects.requests.tabs');
  const pathname = usePathname();

  const tabs = [
    { value: 'invitations', href: ROUTES.PROJECT_INVITATIONS() },
    { value: 'join-requests', href: ROUTES.PROJECT_JOIN_REQUESTS() },
  ] as const;

  const activeTab = tabs.find((tab) => pathname === tab.href)?.value;

  return (
    <div className="flex flex-col gap-4">
      <Tabs value={activeTab}>
        <TabsList variant="line">
          {tabs.map((tab) => (
            <TabsTrigger key={tab.value} value={tab.value} asChild>
              <Link href={tab.href}>{t(tab.value)}</Link>
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      {children}
    </div>
  );
}
