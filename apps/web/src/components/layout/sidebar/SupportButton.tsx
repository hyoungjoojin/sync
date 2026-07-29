'use client';

import * as ChannelService from '@channel.io/channel-web-sdk-loader';
import { ChatCircleDotsIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';

import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';
import { env } from '@/lib/env';

export default function SupportButton() {
  const t = useTranslations('components.layout.sidebar');

  if (!env.NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY) {
    return null;
  }

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <SidebarMenuButton onClick={() => ChannelService.showMessenger()}>
          <ChatCircleDotsIcon />
          <span>{t('support')}</span>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  );
}
