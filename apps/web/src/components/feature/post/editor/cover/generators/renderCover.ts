/**
 * High-level cover rendering: dispatch by algorithm and produce a PNG Blob for
 * upload. Deterministic — identical `params` yield a visually identical PNG.
 *
 * Browser-only (uses `document` / canvas contexts). Call from client code.
 */
import { renderFlow } from './flow';
import type { CoverParams } from './types';
import { COVER_HEIGHT, COVER_WIDTH } from './types';
import { createGLCoverRenderer } from './webgl';

function newCanvas(): HTMLCanvasElement {
  const canvas = document.createElement('canvas');
  canvas.width = COVER_WIDTH;
  canvas.height = COVER_HEIGHT;
  return canvas;
}

function canvasToPngBlob(canvas: HTMLCanvasElement): Promise<Blob> {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) resolve(blob);
      else reject(new Error('cover: canvas.toBlob returned null'));
    }, 'image/png');
  });
}

/**
 * Render `params` onto a fresh offscreen canvas and return its PNG Blob. A new
 * canvas is created per call so the WebGL/2D context type always matches the
 * algorithm.
 */
export async function renderCoverToBlob(params: CoverParams): Promise<Blob> {
  const canvas = newCanvas();
  if (params.algo === 'flow') {
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('cover: 2D context unavailable');
    renderFlow(ctx, params);
    return canvasToPngBlob(canvas);
  }
  const renderer = createGLCoverRenderer(canvas);
  try {
    renderer.render(params);
    return await canvasToPngBlob(canvas);
  } finally {
    renderer.dispose();
  }
}

/** Turn a rendered cover into an uploadable File. */
export async function renderCoverToFile(
  params: CoverParams,
  fileName = 'cover.png',
): Promise<File> {
  const blob = await renderCoverToBlob(params);
  return new File([blob], fileName, { type: 'image/png' });
}

export { COVER_HEIGHT, COVER_WIDTH };
