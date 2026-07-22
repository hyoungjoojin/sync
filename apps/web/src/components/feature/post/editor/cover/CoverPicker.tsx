'use client';

import { ImageIcon, PencilSimpleIcon, TrashIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

import { CoverGallery } from './CoverGallery';
import { type CoverState, coverPreviewUrl } from './coverState';

interface CoverPickerProps {
  value: CoverState;
  onChange: (next: CoverState) => void;
  /** Seed the gallery deterministically (e.g. from the post title). */
  defaultSeed: string;
}

export function CoverPicker({
  value,
  onChange,
  defaultSeed,
}: CoverPickerProps) {
  const t = useTranslations('components.editor.cover');
  const [open, setOpen] = useState(false);
  const previewUrl = coverPreviewUrl(value);
  const selectedParams = value.kind === 'generated' ? value.params : undefined;

  const handleRemove = () => onChange({ kind: 'none' });
  const handleSelect = (result: {
    params: NonNullable<typeof selectedParams>;
    previewUrl: string;
  }) => {
    onChange({
      kind: 'generated',
      params: result.params,
      previewUrl: result.previewUrl,
    });
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <div className="shrink-0">
        {previewUrl ? (
          <div className="group relative w-full overflow-hidden rounded-xl border border-border">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={previewUrl}
              alt={t('alt')}
              className="block aspect-[2.5/1] w-full object-cover"
            />
            <div className="absolute inset-0 flex items-center justify-center gap-2 bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
              <PopoverTrigger asChild>
                <Button type="button" variant="secondary" size="sm">
                  <PencilSimpleIcon size={14} />
                  {t('change')}
                </Button>
              </PopoverTrigger>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={handleRemove}
              >
                <TrashIcon size={14} />
                {t('remove')}
              </Button>
            </div>
          </div>
        ) : (
          <PopoverTrigger asChild>
            <button
              type="button"
              className="flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-border py-3 text-sm font-medium text-muted-foreground transition-colors hover:border-foreground/30 hover:bg-muted/40 hover:text-foreground"
            >
              <ImageIcon size={16} />
              {t('add')}
            </button>
          </PopoverTrigger>
        )}
      </div>

      <PopoverContent
        align="start"
        className="w-[36rem] max-w-[calc(100vw-2rem)] p-0"
      >
        <div className="flex items-center justify-between border-b border-border px-3 py-2">
          <div className="flex gap-1" role="tablist">
            <span
              role="tab"
              aria-selected
              className="rounded-md border border-input bg-background px-2.5 py-1 text-sm font-medium"
            >
              {t('tabs.gallery')}
            </span>
          </div>
          {previewUrl && (
            <button
              type="button"
              onClick={handleRemove}
              className="text-sm text-muted-foreground transition-colors hover:text-foreground"
            >
              {t('remove')}
            </button>
          )}
        </div>

        <div className="max-h-80 overflow-y-auto p-3">
          <CoverGallery
            defaultSeed={defaultSeed}
            selectedParams={selectedParams}
            onSelect={handleSelect}
          />
        </div>
      </PopoverContent>
    </Popover>
  );
}
