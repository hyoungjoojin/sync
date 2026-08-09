'use client';

import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { toast } from 'sonner';

import { getPostBySlug } from '@/api/__generated__/post/post';
import { downloadBlob } from '@/lib/download';
import SyncError, { ErrorCode } from '@/lib/error';

import {
  PostContentUnavailableError,
  createPostArchive,
} from './createPostArchive';

/**
 * 게시물을 마크다운 + 이미지 zip으로 내보낸다. 본문은 캐시를 쓰지 않고 새로
 * 받아온다 — 미디어 URL이 10분짜리 프리사인 URL이라 오래 열어둔 화면의 캐시된
 * URL은 이미 만료됐을 수 있다.
 */
export function useExportPostMarkdown({
  slug,
  postPath,
}: {
  slug: string;
  postPath: string;
}) {
  const t = useTranslations('pages.posts.export');
  const [isExporting, setIsExporting] = useState(false);

  const exportMarkdown = async () => {
    if (isExporting) {
      return;
    }

    setIsExporting(true);

    try {
      const { data: post } = await getPostBySlug(slug);
      const archive = await createPostArchive({
        post,
        url: window.location.origin + postPath,
      });

      downloadBlob(archive.blob, archive.fileName);

      if (archive.failedAssetCount > 0) {
        toast.warning(
          t('messages.partial', { count: archive.failedAssetCount }),
        );
      } else {
        toast.success(t('messages.success'));
      }
    } catch (error) {
      if (error instanceof PostContentUnavailableError) {
        toast.error(t('messages.unavailable'));
      } else if (
        error instanceof SyncError &&
        error.code === ErrorCode.POST_NOT_FOUND
      ) {
        toast.error(t('messages.not-found'));
      } else {
        toast.error(t('messages.error'));
      }
    } finally {
      setIsExporting(false);
    }
  };

  return { exportMarkdown, isExporting };
}
