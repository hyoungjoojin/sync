'use client';

import { useEffect, useRef } from 'react';

import { cn } from '@/lib/utils';

import {
  COVER_HEIGHT,
  COVER_WIDTH,
  type CoverParams,
  type GLCoverRenderer,
  createGLCoverRenderer,
  renderFlow,
} from './generators';

interface CoverCanvasProps {
  params: CoverParams;
  className?: string;
  /** Called with `true` while a (flow) render is in flight, `false` when done. */
  onRenderingChange?: (rendering: boolean) => void;
}

/**
 * Live cover preview. WebGL (aurora/mesh) and Canvas 2D (flow) each need their
 * own canvas element — a canvas is locked to one context type — so both are
 * mounted and the relevant one is shown. Flow renders on the main thread behind
 * a short timeout so the spinner can paint first.
 */
export function CoverCanvas({
  params,
  className,
  onRenderingChange,
}: CoverCanvasProps) {
  const glRef = useRef<HTMLCanvasElement>(null);
  const flowRef = useRef<HTMLCanvasElement>(null);
  const rendererRef = useRef<GLCoverRenderer | null>(null);
  const isFlow = params.algo === 'flow';

  useEffect(() => {
    if (!glRef.current) return;
    try {
      rendererRef.current = createGLCoverRenderer(glRef.current);
    } catch {
      rendererRef.current = null;
    }
    return () => {
      rendererRef.current?.dispose();
      rendererRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!isFlow) {
      rendererRef.current?.render(params);
      return;
    }
    const ctx = flowRef.current?.getContext('2d');
    if (!ctx) return;
    onRenderingChange?.(true);
    const id = window.setTimeout(() => {
      renderFlow(ctx, params);
      onRenderingChange?.(false);
    }, 24);
    return () => window.clearTimeout(id);
  }, [params, isFlow, onRenderingChange]);

  return (
    <div
      className={cn(
        'relative w-full overflow-hidden rounded-lg bg-black',
        className,
      )}
      style={{ aspectRatio: `${COVER_WIDTH} / ${COVER_HEIGHT}` }}
    >
      <canvas
        ref={glRef}
        width={COVER_WIDTH}
        height={COVER_HEIGHT}
        hidden={isFlow}
        className="block h-full w-full"
      />
      <canvas
        ref={flowRef}
        width={COVER_WIDTH}
        height={COVER_HEIGHT}
        hidden={!isFlow}
        className="block h-full w-full"
      />
    </div>
  );
}
