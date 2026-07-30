'use client';

import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { toast } from 'sonner';

import { downloadBlob } from '@/lib/download';

import { fetchMediaBlob } from './postMediaSource';

/**
 * 첨부 파일을 내려받는다. S3 키는 UUID여서 원본 파일 이름을 함께 넘겨야 한다.
 */
export function useDownloadPostMedia(slug: string | null) {
  const t = useTranslations('components.editor.file');
  const [downloadingMediaId, setDownloadingMediaId] = useState<string | null>(
    null,
  );

  const download = async (mediaId: string, fileName: string | null) => {
    if (!slug || downloadingMediaId !== null) {
      return;
    }

    setDownloadingMediaId(mediaId);

    try {
      const blob = await fetchMediaBlob({ mediaId, url: null, slug });

      downloadBlob(blob, fileName ?? mediaId);
    } catch {
      toast.error(t('errors.download-failed'));
    } finally {
      setDownloadingMediaId(null);
    }
  };

  return { download, downloadingMediaId };
}
