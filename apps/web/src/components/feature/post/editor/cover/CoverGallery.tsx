'use client';

import { ArrowsClockwiseIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { cn } from '@/lib/utils';

import { CoverCanvas } from './CoverCanvas';
import {
  COVER_ALGOS,
  type CoverAlgo,
  type CoverParams,
  randomCoverParams,
  renderCoverToBlob,
} from './generators';

interface CoverGalleryProps {
  /** Seed the preview deterministically the first time it opens. */
  defaultSeed: string;
  /** The currently-selected cover params, if any (to resume from). */
  selectedParams?: CoverParams;
  onSelect: (result: { params: CoverParams; previewUrl: string }) => void;
}

function randomSeed(): string {
  return (
    Math.floor(Math.random() * 0xffffffff).toString(36) +
    '-' +
    Math.floor(Math.random() * 0xffff).toString(36)
  );
}

/**
 * The gallery tab of the cover dropdown: a single live preview plus a small
 * selector for the three generator styles and a re-roll. "Use" renders the
 * current cover locally (no upload — that happens on save) and hands it back.
 */
export function CoverGallery({
  defaultSeed,
  selectedParams,
  onSelect,
}: CoverGalleryProps) {
  const t = useTranslations('components.editor.cover');
  const [params, setParams] = useState<CoverParams>(
    () => selectedParams ?? randomCoverParams(defaultSeed),
  );
  const [rendering, setRendering] = useState(false);
  const [applying, setApplying] = useState(false);

  const setAlgo = (algo: CoverAlgo) => setParams((prev) => ({ ...prev, algo }));
  // Re-roll re-samples every knob (mood/hue/complexity/grain/vignette) within
  // its bounds while keeping the algorithm the author selected.
  const reroll = () =>
    setParams((prev) => ({
      ...randomCoverParams(randomSeed()),
      algo: prev.algo,
    }));

  async function handleUse() {
    setApplying(true);
    try {
      const blob = await renderCoverToBlob(params);
      onSelect({ params, previewUrl: URL.createObjectURL(blob) });
    } finally {
      setApplying(false);
    }
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center gap-1.5">
        {COVER_ALGOS.map((algo) => (
          <button
            key={algo}
            type="button"
            onClick={() => setAlgo(algo)}
            className={cn(
              'rounded-md border px-2.5 py-1 text-sm font-medium transition-colors',
              params.algo === algo
                ? 'border-primary bg-primary/5 text-primary'
                : 'border-border text-muted-foreground hover:bg-muted/50',
            )}
          >
            {t(`algo.${algo}`)}
          </button>
        ))}
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="ml-auto"
          onClick={reroll}
        >
          <ArrowsClockwiseIcon size={14} />
          {t('gallery.reroll')}
        </Button>
      </div>

      <div className="relative">
        <CoverCanvas params={params} onRenderingChange={setRendering} />
        {rendering && (
          <div className="pointer-events-none absolute inset-0 grid place-items-center rounded-lg bg-black/30">
            <Spinner className="border-white size-5" />
          </div>
        )}
      </div>

      <Button
        type="button"
        onClick={handleUse}
        disabled={applying || rendering}
      >
        {applying && <Spinner className="border-white size-3" />}
        {t('gallery.use')}
      </Button>
    </div>
  );
}
