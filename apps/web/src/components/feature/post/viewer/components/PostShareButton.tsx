'use client';

import {
  EnvelopeSimpleIcon,
  FacebookLogoIcon,
  type Icon,
  LinkSimpleIcon,
  LinkedinLogoIcon,
  ShareNetworkIcon,
  XLogoIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useSyncExternalStore } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

import { PostStatus } from '../../types/post';
import { useCopyPostLink } from '../hooks/useCopyPostLink';
import type { PostSummary } from '../types';

const SHARE_TITLE_LIMIT = 80;

function buildShareUrls(url: string, title: string) {
  const link = encodeURIComponent(url);
  const text = encodeURIComponent(title);

  return {
    x: `https://x.com/intent/post?text=${text}&url=${link}`,
    linkedin: `https://www.linkedin.com/sharing/share-offsite/?url=${link}`,
    facebook: `https://www.facebook.com/sharer/sharer.php?u=${link}`,
    email: `mailto:?subject=${text}&body=${link}`,
  };
}

type ShareTarget = keyof ReturnType<typeof buildShareUrls>;

const EXTERNAL_TARGETS: { target: ShareTarget; Icon: Icon }[] = [
  { target: 'x', Icon: XLogoIcon },
  { target: 'linkedin', Icon: LinkedinLogoIcon },
  { target: 'facebook', Icon: FacebookLogoIcon },
  { target: 'email', Icon: EnvelopeSimpleIcon },
];

const subscribeToNothing = () => () => {};

export function PostShareButton({
  summary,
  postPath,
  onClick,
}: {
  summary: PostSummary;
  postPath: string;
  onClick?: (event: React.MouseEvent) => void;
}) {
  const t = useTranslations('pages.posts.share');
  const tCopyLink = useTranslations('pages.posts.copy-link');
  const copyLink = useCopyPostLink(postPath);
  // navigator.share 존재 여부는 서버에서 알 수 없어 하이드레이션 이후에 확인한다.
  const canUseNativeShare = useSyncExternalStore(
    subscribeToNothing,
    () => typeof navigator !== 'undefined' && 'share' in navigator,
    () => false,
  );

  const shareTitle = (
    summary.title?.trim() ||
    summary.preview?.trim() ||
    ''
  ).slice(0, SHARE_TITLE_LIMIT);

  // 초안은 다른 사용자가 열 수 없으므로 외부 공유 대상을 노출하지 않는다.
  const isShareable = summary.status === PostStatus.PUBLISHED;

  const getShareUrl = () => window.location.origin + postPath;

  const openShareTarget = (target: ShareTarget) => {
    const url = buildShareUrls(getShareUrl(), shareTitle)[target];

    if (target === 'email') {
      window.location.assign(url);
      return;
    }

    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const handleNativeShare = async () => {
    try {
      await navigator.share({ title: shareTitle, url: getShareUrl() });
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return;
      }

      toast.error(t('messages.error'));
    }
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon-sm"
          aria-label={t('trigger')}
          onClick={onClick}
        >
          <ShareNetworkIcon />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" onClick={onClick}>
        <DropdownMenuItem onSelect={copyLink}>
          <LinkSimpleIcon />
          {tCopyLink('trigger')}
        </DropdownMenuItem>

        {isShareable && canUseNativeShare && (
          <DropdownMenuItem onSelect={handleNativeShare}>
            <ShareNetworkIcon />
            {t('targets.device')}
          </DropdownMenuItem>
        )}

        {isShareable && (
          <>
            <DropdownMenuSeparator />

            {EXTERNAL_TARGETS.map(({ target, Icon: TargetIcon }) => (
              <DropdownMenuItem
                key={target}
                onSelect={() => openShareTarget(target)}
              >
                <TargetIcon />
                {t(`targets.${target}`)}
              </DropdownMenuItem>
            ))}
          </>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
