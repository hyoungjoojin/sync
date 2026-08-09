'use client';

import { useTranslations } from 'next-intl';
import { useEffect, useState } from 'react';

import { Spinner } from '@/components/ui/spinner';

/** 20MB 짜리 텍스트를 그대로 그리면 화면이 멈추므로 앞부분만 보여준다. */
const MAX_PREVIEW_CHARACTERS = 200_000;

export function TextPreview({ blob }: { blob: Blob }) {
  const t = useTranslations('components.editor.file.preview');

  const [text, setText] = useState<string | null>(null);
  const [isTruncated, setIsTruncated] = useState(false);

  useEffect(() => {
    let cancelled = false;

    blob.text().then((content) => {
      if (cancelled) {
        return;
      }

      setText(content.slice(0, MAX_PREVIEW_CHARACTERS));
      setIsTruncated(content.length > MAX_PREVIEW_CHARACTERS);
    });

    return () => {
      cancelled = true;
    };
  }, [blob]);

  if (text === null) {
    return (
      <div className="flex justify-center py-6">
        <Spinner className="size-5" />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      <pre className="max-h-120 overflow-auto whitespace-pre-wrap break-words rounded-md bg-muted p-3 text-xs">
        {text}
      </pre>

      {isTruncated && (
        <p className="text-xs text-muted-foreground">{t('truncated')}</p>
      )}
    </div>
  );
}
