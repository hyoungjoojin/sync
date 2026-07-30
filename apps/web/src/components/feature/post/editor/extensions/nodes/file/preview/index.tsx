'use client';

import { useTranslations } from 'next-intl';
import dynamic from 'next/dynamic';
import { useEffect, useState } from 'react';

import { fetchMediaBlob } from '@/components/feature/post/hooks/postMediaSource';
import { Spinner } from '@/components/ui/spinner';
import { isPdfMediaType, isTextMediaType } from '@/lib/tiptap-utils';

// react-pdf 와 pdfjs 는 무거우므로 미리보기를 실제로 펼칠 때만 받아온다.
const PdfPreview = dynamic(
  () => import('./PdfPreview').then((module) => module.PdfPreview),
  { ssr: false, loading: () => <PreviewSpinner /> },
);

const TextPreview = dynamic(
  () => import('./TextPreview').then((module) => module.TextPreview),
  { ssr: false, loading: () => <PreviewSpinner /> },
);

function PreviewSpinner() {
  return (
    <div className="flex justify-center py-6">
      <Spinner className="size-5" />
    </div>
  );
}

export function FilePreview({
  mediaId,
  mediaType,
  url,
  slug,
  localFile = null,
}: {
  mediaId: string;
  mediaType: string;
  url: string | null;
  slug: string | null;
  /** 아직 저장되지 않은 글에서 방금 올린 원본. 있으면 내려받지 않는다. */
  localFile?: File | null;
}) {
  const t = useTranslations('components.editor.file.preview');

  // 어느 원본에서 받아온 결과인지 함께 들고 있으면, 원본이 바뀌었을 때 효과 안에서
  // 상태를 되돌리지 않고도 그리는 시점에 지난 결과를 걸러낼 수 있다.
  const source = `${mediaId}:${url ?? ''}`;
  const [loaded, setLoaded] = useState<{
    source: string;
    blob: Blob | null;
  } | null>(null);

  useEffect(() => {
    if (localFile) {
      return;
    }

    const controller = new AbortController();

    fetchMediaBlob({ mediaId, url, slug, signal: controller.signal })
      .then((blob) => {
        if (!controller.signal.aborted) {
          setLoaded({ source, blob });
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setLoaded({ source, blob: null });
        }
      });

    return () => controller.abort();
  }, [mediaId, url, slug, source, localFile]);

  const current = localFile
    ? { source, blob: localFile as Blob }
    : loaded?.source === source
      ? loaded
      : null;

  if (current === null) {
    return <PreviewSpinner />;
  }

  if (current.blob === null) {
    return (
      <p className="py-6 text-center text-sm text-destructive">
        {t('unavailable')}
      </p>
    );
  }

  if (isPdfMediaType(mediaType)) {
    return <PdfPreview blob={current.blob} />;
  }

  if (isTextMediaType(mediaType)) {
    return <TextPreview blob={current.blob} />;
  }

  return null;
}
