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

const WEBSOCKET_ENABLED = process.env.NEXT_PUBLIC_WEBSOCKET_ENABLED === 'true';

/**
 * 알림 목록 조회와 WebSocket 실시간 구독을 함께 처리하는 공용 훅.
 * 드롭다운 미리보기와 전체 알림 페이지가 동일한 구독 로직을 공유한다.
 *
 * WebSocket이 꺼져 있으면 구독을 건너뛴다. `subscribe`는 클라이언트가 연결되지
 * 않았을 때 오류 토스트를 띄우는데, 설정으로 꺼둔 상태는 오류가 아니며 이때는
 * 폴링으로 알림을 가져온다.
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
    if (!WEBSOCKET_ENABLED || !session?.user.id) {
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
