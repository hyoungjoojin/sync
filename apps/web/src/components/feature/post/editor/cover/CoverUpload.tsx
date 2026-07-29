'use client';

import { ImageIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { FileInput, type FileInputError } from '@/components/ui/input';

const COVER_UPLOAD_ALLOWED_TYPES = 'image/*';
const COVER_UPLOAD_MAX_SIZE_BYTES = 5 * 1024 * 1024;

interface CoverUploadProps {
  onSelect: (result: { file: File; previewUrl: string }) => void;
}

/**
 * The upload tab of the cover dropdown: the author picks an image from their
 * device and it is kept locally (file + object URL) until the post is saved,
 * mirroring how a gallery-picked cover defers its upload.
 */
export function CoverUpload({ onSelect }: CoverUploadProps) {
  const t = useTranslations('components.editor.cover');
  const [error, setError] = useState<FileInputError | null>(null);

  const handleFileChange = (files: File[]) => {
    const file = files[0];
    if (!file) return;
    setError(null);
    onSelect({ file, previewUrl: URL.createObjectURL(file) });
  };

  return (
    <div className="flex flex-col gap-3">
      <FileInput
        accept={COVER_UPLOAD_ALLOWED_TYPES}
        maxSize={COVER_UPLOAD_MAX_SIZE_BYTES}
        maxFiles={1}
        onFileChange={handleFileChange}
        onError={setError}
      >
        <div className="flex aspect-[2.5/1] w-full flex-col items-center justify-center gap-2 rounded-lg border border-dashed border-border bg-muted/40 text-muted-foreground transition-colors hover:border-foreground/30 hover:bg-muted/60 hover:text-foreground">
          <ImageIcon size={28} />
          <span className="text-sm font-medium">{t('upload.prompt')}</span>
          <span className="text-xs">{t('upload.hint')}</span>
        </div>
      </FileInput>

      {error && (
        <p className="text-sm text-destructive">
          {t(`upload.errors.${error}`)}
        </p>
      )}
    </div>
  );
}
