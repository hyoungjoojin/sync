'use client';

import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';

import {
  getGetNotificationsQueryKey,
  useGetNotifications,
} from '@/api/__generated__/notification/notification';
import type { GetNotificationsParams } from '@/api/__generated__/types/GetNotificationsParams';
import type { GetNotificationsResponse } from '@/api/__generated__/types/GetNotificationsResponse';
import type { GetNotificationsResponseNotificationsContentItem } from '@/api/__generated__/types/GetNotificationsResponseNotificationsContentItem';
import { useWebSocket } from '@/components/providers/WebSocketProvider';
import { useSession } from '@/lib/auth/client';

/**
 * 알림 목록 조회와 WebSocket 실시간 구독을 함께 처리하는 공용 훅.
 * 드롭다운 미리보기와 전체 알림 페이지가 동일한 구독 로직을 공유한다.
 */
export function useNotifications(params: GetNotificationsParams) {
  const { page, size } = params;
  const { data: session } = useSession();
  const { subscribe } = useWebSocket();
  const queryClient = useQueryClient();

  const query = useGetNotifications(params, {
    query: { enabled: !!session?.user.id },
  });

  useEffect(() => {
    if (!session?.user.id) {
      return;
    }

    const subscription = subscribe(
      `/topic/notifications/${session.user.id}`,
      (message) => {
        const notification = JSON.parse(
          message.body,
        ) as GetNotificationsResponseNotificationsContentItem;

        queryClient.setQueryData<{ data: GetNotificationsResponse }>(
          getGetNotificationsQueryKey({ page, size }),
          (old) => {
            if (!old) {
              return old;
            }

            const previous = old.data.notifications;

            return {
              ...old,
              data: {
                ...old.data,
                unreadCount: old.data.unreadCount + 1,
                notifications: previous
                  ? {
                      ...previous,
                      content: [notification, ...(previous.content ?? [])],
                    }
                  : previous,
              },
            };
          },
        );
      },
    );

    return () => {
      subscription?.unsubscribe();
    };
  }, [session?.user.id, queryClient, page, size, subscribe]);

  return query;
}
