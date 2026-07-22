/**
 * `flow` algorithm — thousands of particles advected through a Perlin vector
 * field, drawn as faint additive strokes over a dark ground. Canvas 2D, one
 * shot (no animation). Fully deterministic from `params.seed`, including grain.
 */
import { buildBg, buildPalette, deriveSeed, rgbCss } from './palette';
import { mulberry32, strHash } from './rng';
import type { CoverParams } from './types';

/** Classic Perlin 2D with a seeded permutation table. Range ≈ [-1, 1]. */
function makePerlin(rand: () => number): (x: number, y: number) => number {
  const perm: number[] = [];
  for (let i = 0; i < 256; i++) perm[i] = i;
  for (let i = 255; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1));
    const t = perm[i]!;
    perm[i] = perm[j]!;
    perm[j] = t;
  }
  const p = new Uint8Array(512);
  for (let i = 0; i < 512; i++) p[i] = perm[i & 255]!;
  const fade = (t: number): number => t * t * t * (t * (t * 6 - 15) + 10);
  const lerp = (a: number, b: number, t: number): number => a + t * (b - a);
  const grad = (h: number, x: number, y: number): number => {
    const u = (h & 1) === 0 ? x : -x;
    const v = (h & 2) === 0 ? y : -y;
    return u + v;
  };
  return (x: number, y: number): number => {
    const X = Math.floor(x) & 255;
    const Y = Math.floor(y) & 255;
    const xf = x - Math.floor(x);
    const yf = y - Math.floor(y);
    const u = fade(xf);
    const v = fade(yf);
    const aa = p[p[X]! + Y]!;
    const ab = p[p[X]! + Y + 1]!;
    const ba = p[p[X + 1]! + Y]!;
    const bb = p[p[X + 1]! + Y + 1]!;
    return lerp(
      lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u),
      lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u),
      v,
    );
  };
}

function drawGrain(
  ctx: CanvasRenderingContext2D,
  rand: () => number,
  amount: number,
): void {
  if (amount <= 0) return;
  const W = ctx.canvas.width;
  const H = ctx.canvas.height;
  const nc = document.createElement('canvas');
  nc.width = W >> 1;
  nc.height = H >> 1;
  const nx = nc.getContext('2d');
  if (!nx) return;
  const img = nx.createImageData(nc.width, nc.height);
  for (let i = 0; i < img.data.length; i += 4) {
    const value = (rand() * 255) | 0;
    img.data[i] = value;
    img.data[i + 1] = value;
    img.data[i + 2] = value;
    img.data[i + 3] = 255;
  }
  nx.putImageData(img, 0, 0);
  ctx.save();
  ctx.globalAlpha = Math.min(0.9, amount * 3.2);
  ctx.globalCompositeOperation = 'overlay';
  ctx.drawImage(nc, 0, 0, W, H);
  ctx.restore();
}

function drawVignette(ctx: CanvasRenderingContext2D, amount: number): void {
  if (amount <= 0) return;
  const W = ctx.canvas.width;
  const H = ctx.canvas.height;
  const g = ctx.createRadialGradient(
    W / 2,
    H / 2,
    Math.min(W, H) * 0.22,
    W / 2,
    H / 2,
    Math.max(W, H) * 0.72,
  );
  g.addColorStop(0, 'rgba(0,0,0,0)');
  g.addColorStop(1, `rgba(0,0,0,${amount})`);
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, W, H);
}

// Fixed generation constants — never device-derived, so the same seed yields
// the same cover on every device (determinism invariant).
const PARTICLES = 2600;
const STEPS = 130;
const TURNS = Math.PI * 3.0;
const STEP_LEN = 1.7;

export function renderFlow(
  ctx: CanvasRenderingContext2D,
  params: CoverParams,
): void {
  const W = ctx.canvas.width;
  const H = ctx.canvas.height;
  const d = deriveSeed(params.seed);
  const pal = buildPalette(d, params.hueShift, params.mood);
  const bg = buildBg(d, params.hueShift, params.mood);
  const rand = mulberry32(strHash(`${params.seed}|flow`));
  const perlin = makePerlin(rand);

  ctx.globalCompositeOperation = 'source-over';
  ctx.globalAlpha = 1;
  ctx.fillStyle = rgbCss(bg);
  ctx.fillRect(0, 0, W, H);

  const scale = 0.0013 * (1.0 + params.complexity * 1.7);
  ctx.lineWidth = 1.15;
  ctx.lineCap = 'round';
  ctx.globalAlpha = 0.055;
  ctx.globalCompositeOperation = 'lighter';
  for (let n = 0; n < PARTICLES; n++) {
    let x = rand() * W;
    let y = rand() * H;
    const ci = (rand() * pal.length) | 0;
    ctx.strokeStyle = rgbCss(pal[ci] ?? pal[0]);
    ctx.beginPath();
    ctx.moveTo(x, y);
    for (let s = 0; s < STEPS; s++) {
      const ang = perlin(x * scale, y * scale) * TURNS;
      x += Math.cos(ang) * STEP_LEN;
      y += Math.sin(ang) * STEP_LEN;
      if (x < -5 || x > W + 5 || y < -5 || y > H + 5) break;
      ctx.lineTo(x, y);
    }
    ctx.stroke();
  }
  ctx.globalCompositeOperation = 'source-over';
  ctx.globalAlpha = 1;

  // Grain uses a fresh seeded stream so it is stable per seed but independent
  // of how many particle draws consumed the main stream.
  drawGrain(ctx, mulberry32(strHash(`${params.seed}|grain`)), params.grain);
  drawVignette(ctx, params.vignette);
}
