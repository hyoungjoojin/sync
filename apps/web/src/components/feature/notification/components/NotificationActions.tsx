'use client';

import { useTranslations } from 'next-intl';
import { toast } from 'sonner';

import {
  useAcceptProjectInvitation,
  useDeclineProjectInvitation,
} from '@/api/__generated__/project/project';
import type { GetNotificationsResponseNotificationsContentItem } from '@/api/__generated__/types/GetNotificationsResponseNotificationsContentItem';
import {
  useApproveJoinRequest,
  useDeclineJoinRequest,
} from '@/components/feature/project/hooks/useProjectJoinRequest';
import { Button } from '@/components/ui/button';
import SyncError, { ErrorCode } from '@/lib/error';

interface NotificationActionsProps {
  notification: GetNotificationsResponseNotificationsContentItem;
  onSettled: () => void;
}

/**
 * 알림 자체에서 바로 처리할 수 있는 요청에 대해 버튼을 렌더링한다.
 * 처리 대상이 없는 알림 유형이면 아무것도 그리지 않는다.
 */
export default function NotificationActions({
  notification,
  onSettled,
}: NotificationActionsProps) {
  const { payload } = notification;

  if (payload.type === 'PROJECT_INVITATION') {
    return <InvitationActions token={payload.token} onSettled={onSettled} />;
  }

  if (payload.type === 'PROJECT_JOIN_REQUEST') {
    // 요청 행이 사라지면 처리할 대상도 없으므로 entityId 없이는 버튼을 숨긴다.
    if (notification.entityId == null) {
      return null;
    }

    return (
      <JoinRequestActions
        handle={payload.projectHandle}
        requestId={String(notification.entityId)}
        onSettled={onSettled}
      />
    );
  }

  return null;
}

function InvitationActions({
  token,
  onSettled,
}: {
  token: string;
  onSettled: () => void;
}) {
  const t = useTranslations('components.navigation.notifications.actions');

  const { mutate: accept, isPending: isAccepting } =
    useAcceptProjectInvitation();
  const { mutate: decline, isPending: isDeclining } =
    useDeclineProjectInvitation();

  const isBusy = isAccepting || isDeclining;

  /**
   * 이미 수락·거절했거나 만료된 초대는 알림에 그대로 남아 있다. 서버가 두 경우
   * 모두 같은 예외로 응답하므로, 실패를 오류가 아니라 "이미 처리됨"으로 알린다.
   */
  const handleError = (error: unknown) => {
    if (error instanceof SyncError) {
      switch (error.code) {
        case ErrorCode.PROJECT_INVITATION_NOT_FOUND:
          toast.info(t('invitation.already-handled'));
          return;
        case ErrorCode.PROJECT_INVITATION_EXPIRED:
          toast.error(t('invitation.expired'));
          return;
      }
    }

    toast.error(t('error'));
  };

  return (
    <ActionRow>
      <Button
        size="sm"
        variant="outline"
        disabled={isBusy}
        onClick={() =>
          decline(
            { token },
            {
              onSuccess: () => toast.success(t('invitation.declined')),
              onError: handleError,
              onSettled,
            },
          )
        }
      >
        {t('invitation.decline')}
      </Button>
      <Button
        size="sm"
        disabled={isBusy}
        onClick={() =>
          accept(
            { token },
            {
              onSuccess: () => toast.success(t('invitation.accepted')),
              onError: handleError,
              onSettled,
            },
          )
        }
      >
        {t('invitation.accept')}
      </Button>
    </ActionRow>
  );
}

function JoinRequestActions({
  handle,
  requestId,
  onSettled,
}: {
  handle: string;
  requestId: string;
  onSettled: () => void;
}) {
  const t = useTranslations('components.navigation.notifications.actions');

  const { mutate: approve, isPending: isApproving } = useApproveJoinRequest();
  const { mutate: decline, isPending: isDeclining } = useDeclineJoinRequest();

  const isBusy = isApproving || isDeclining;

  /**
   * 관리자가 여러 명이므로 다른 관리자가 먼저 처리했을 수 있다. 그 경우 요청 행은
   * 이미 삭제된 상태라 오류가 아닌 안내로 처리한다.
   */
  const handleError = (error: unknown) => {
    if (
      error instanceof SyncError &&
      error.code === ErrorCode.PROJECT_JOIN_REQUEST_NOT_FOUND
    ) {
      toast.info(t('join-request.already-handled'));
      return;
    }

    toast.error(t('error'));
  };

  return (
    <ActionRow>
      <Button
        size="sm"
        variant="outline"
        disabled={isBusy}
        onClick={() =>
          decline(
            { handle, requestId },
            {
              onSuccess: () => toast.success(t('join-request.declined')),
              onError: handleError,
              onSettled,
            },
          )
        }
      >
        {t('join-request.decline')}
      </Button>
      <Button
        size="sm"
        disabled={isBusy}
        onClick={() =>
          approve(
            { handle, requestId },
            {
              onSuccess: () => toast.success(t('join-request.approved')),
              onError: handleError,
              onSettled,
            },
          )
        }
      >
        {t('join-request.approve')}
      </Button>
    </ActionRow>
  );
}

function ActionRow({ children }: { children: React.ReactNode }) {
  return <div className="flex justify-end gap-2 pt-1">{children}</div>;
}
