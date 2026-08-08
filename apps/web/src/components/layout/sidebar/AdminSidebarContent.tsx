'use client';

import {
  ArrowLeftIcon,
  FlagIcon,
  GiftIcon,
  TagIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

import {
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';
import ROUTES from '@/util/routes';

import SidebarCloseButton from './SidebarCloseButton';

const nav = [
  {
    labelKey: 'admin.reports',
    href: ROUTES.ADMIN_POST_REPORTS(),
    icon: FlagIcon,
  },
  {
    labelKey: 'admin.promotions',
    href: ROUTES.ADMIN_PROMOTIONS(),
    icon: GiftIcon,
  },
  {
    labelKey: 'admin.tags',
    href: ROUTES.ADMIN_TAGS(),
    icon: TagIcon,
  },
] as const;

export default function AdminSidebarContent() {
  const t = useTranslations('components.layout.sidebar');
  const pathname = usePathname();

  return (
    <>
      <SidebarHeader className="flex flex-row items-center justify-between p-4">
        <SidebarCloseButton />
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton asChild className="text-sidebar-foreground/70">
                <Link href={ROUTES.HOME()}>
                  <ArrowLeftIcon />
                  {t('back-to-home')}
                </Link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupLabel>{t('admin.title')}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {nav.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);

                return (
                  <SidebarMenuItem key={item.href}>
                    <SidebarMenuButton asChild isActive={isActive}>
                      <Link href={item.href}>
                        <Icon />
                        {t(item.labelKey)}
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </>
  );
}
