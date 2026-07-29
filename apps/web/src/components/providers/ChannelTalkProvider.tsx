'use client';

import * as ChannelService from '@channel.io/channel-web-sdk-loader';
import { useEffect } from 'react';

import { useGetChannelTalkIdentity } from '@/api/__generated__/channel-talk/channel-talk';
import { useSession } from '@/lib/auth/client';
import { isAuthenticated } from '@/lib/auth/utils';
import { env } from '@/lib/env';

const pluginKey = env.NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY;

export default function ChannelTalkProvider() {
  const { data: session, isPending: isSessionPending } = useSession();
  const authenticated = isAuthenticated(session);

  const { data, isPending: isIdentityPending } = useGetChannelTalkIdentity({
    query: {
      enabled: Boolean(pluginKey) && !isSessionPending && authenticated,
      staleTime: Infinity,
      retry: false,
    },
  });

  const identity = data?.data;

  // memberHash 없이 memberId만 보내면 채널톡이 검증을 건너뛰므로, 둘 다 없는
  // 완전한 익명 부팅으로 떨어뜨린다.
  const memberId = identity?.memberHash ? identity.memberId : undefined;
  const memberHash = identity?.memberHash ?? undefined;

  const isIdentityResolved = !authenticated || !isIdentityPending;

  useEffect(() => {
    if (!pluginKey || isSessionPending || !isIdentityResolved) {
      return;
    }

    ChannelService.loadScript();
    ChannelService.boot({
      pluginKey,
      memberId,
      memberHash,
      hideChannelButtonOnBoot: true,
      hidePopup: true,
    });

    return () => {
      ChannelService.shutdown();
    };
  }, [isSessionPending, isIdentityResolved, memberId, memberHash]);

  return null;
}
