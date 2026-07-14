'use client';

import { useTranslations } from 'next-intl';
import Link from 'next/link';

import type { GetNotificationsResponseNotificationsContentItem } from '@/api/__generated__/types/GetNotificationsResponseNotificationsContentItem';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

interface NotificationItemProps {
  notification: GetNotificationsResponseNotificationsContentItem;
  onRead: (id: number) => void;
}

export default function NotificationItem({
  notification,
  onRead,
}: NotificationItemProps) {
  const t = useTranslations('components.navigation.notifications.types');
  const { payload, actor, status } = notification;

  const { href, message } = resolve(payload, t);

  const body = (
    <div className="flex items-start gap-3">
      {actor && (
        <Avatar size="sm" className="mt-0.5">
          <AvatarImage src={actor.profileImageUrl ?? undefined} />
          <AvatarFallback>{actor.name?.[0]}</AvatarFallback>
        </Avatar>
      )}
      <p className="flex-1 text-sm">{message}</p>
      {status === 'UNREAD' && (
        <span className="mt-1.5 size-2 shrink-0 rounded-full bg-primary" />
      )}
    </div>
  );

  const className = cn(
    'block w-full rounded-md px-3 py-2 text-left hover:bg-muted',
    status === 'UNREAD' && 'bg-muted/50',
  );

  if (!href) {
    return (
      <button
        type="button"
        className={className}
        onClick={() => onRead(notification.id)}
      >
        {body}
      </button>
    );
  }

  return (
    <Link
      href={href}
      className={className}
      onClick={() => onRead(notification.id)}
    >
      {body}
    </Link>
  );
}

function resolve(
  payload: GetNotificationsResponseNotificationsContentItem['payload'],
  t: ReturnType<
    typeof useTranslations<'components.navigation.notifications.types'>
  >,
): { href: string | null; message: string } {
  switch (payload.type) {
    case 'NEW_COMMENT':
      return {
        href: ROUTES.POST(payload.postSlug),
        message: t('NEW_COMMENT', {
          name: payload.actorName,
          title: payload.postTitle,
        }),
      };
    case 'NEW_FOLLOWER':
      return {
        href: ROUTES.PROFILE(payload.actorHandle),
        message: t('NEW_FOLLOWER', { name: payload.actorName }),
      };
    case 'PROJECT_INVITATION':
      return {
        href: ROUTES.PROJECT_INVITATIONS(),
        message: t('PROJECT_INVITATION', {
          name: payload.actorName,
          project: payload.projectName,
        }),
      };
    case 'NEW_MESSAGE':
      // 실시간 메시지 페이지는 아직 라우팅되지 않아(_legacy) 링크 없이 내용만 표시한다.
      return {
        href: null,
        message: t('NEW_MESSAGE', { name: payload.actorName }),
      };
    case 'WELCOME':
      return { href: null, message: t('WELCOME') };
    default:
      payload satisfies never;
      return { href: null, message: '' };
  }
}
