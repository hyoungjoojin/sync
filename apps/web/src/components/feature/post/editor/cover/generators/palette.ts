/**
 * Signature palette system.
 *
 * Every cover derives its colors in OKLCH from a single seeded base hue with
 * fixed hue rotations and a lightness curve, so covers across the app read as
 * siblings regardless of algorithm. `mood` reshapes chroma/lightness without
 * touching the seed.
 */
import { clamp01, mulberry32, strHash } from './rng';
import type { CoverMood, Palette, Rgb, Vec2 } from './types';

/** OKLCH → sRGB (0..1), gamma-encoded. Standard Björn Ottosson matrices. */
export function oklch(L: number, C: number, hDeg: number): Rgb {
  const h = (hDeg * Math.PI) / 180;
  const a = C * Math.cos(h);
  const b = C * Math.sin(h);
  const l_ = L + 0.3963377774 * a + 0.2158037573 * b;
  const m_ = L - 0.1055613458 * a - 0.0638541728 * b;
  const s_ = L - 0.0894841775 * a - 1.291485548 * b;
  const l = l_ * l_ * l_;
  const m = m_ * m_ * m_;
  const s = s_ * s_ * s_;
  const r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s;
  const g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s;
  const bl = -0.0041960863 * l - 0.7034186147 * m + 1.707614701 * s;
  const gamma = (c: number): number => {
    const x = clamp01(c);
    return x <= 0.0031308 ? 12.92 * x : 1.055 * Math.pow(x, 1 / 2.4) - 0.055;
  };
  return [gamma(r), gamma(g), gamma(bl)];
}

export function rgbCss(c: Rgb): string {
  return `rgb(${Math.round(c[0] * 255)},${Math.round(c[1] * 255)},${Math.round(
    c[2] * 255,
  )})`;
}

interface Stop {
  dh: number;
  L: number;
  C: number;
}

const STOPS: readonly [Stop, Stop, Stop, Stop] = [
  { dh: -30, L: 0.6, C: 0.12 },
  { dh: 4, L: 0.72, C: 0.16 },
  { dh: 26, L: 0.66, C: 0.14 },
  { dh: 58, L: 0.82, C: 0.1 },
];

const MOODS: Record<CoverMood, { cMul: number; lAdd: number }> = {
  soft: { cMul: 1.0, lAdd: 0 },
  vivid: { cMul: 1.35, lAdd: 0.02 },
  muted: { cMul: 0.55, lAdd: 0.04 },
  deep: { cMul: 0.95, lAdd: -0.13 },
};

/** Seed-derived, mood/hue-independent structure: base hue, jitter, mesh points. */
export interface CoverSeedData {
  baseHue: number;
  jitter: { dh: readonly number[]; dL: readonly number[] };
  points: readonly Vec2[];
}

export function deriveSeed(seed: string): CoverSeedData {
  const rand = mulberry32(strHash(seed));
  const baseHue = rand() * 360;
  const jitter = {
    dh: STOPS.map(() => (rand() * 2 - 1) * 9),
    dL: STOPS.map(() => (rand() * 2 - 1) * 0.04),
  };
  const points: Vec2[] = [];
  for (let i = 0; i < 6; i++) {
    points.push([0.08 + rand() * 0.84, 0.12 + rand() * 0.76]);
  }
  return { baseHue, jitter, points };
}

export function buildPalette(
  d: CoverSeedData,
  hueShift: number,
  mood: CoverMood,
): Palette {
  const m = MOODS[mood];
  const stop = (i: 0 | 1 | 2 | 3): Rgb => {
    const s = STOPS[i];
    const dL = d.jitter.dL[i] ?? 0;
    const dh = d.jitter.dh[i] ?? 0;
    return oklch(
      clamp01(s.L + m.lAdd + dL),
      Math.max(0, s.C * m.cMul),
      d.baseHue + hueShift + s.dh + dh,
    );
  };
  return [stop(0), stop(1), stop(2), stop(3)];
}

/** Dark grounding color used behind flow strokes and as a mesh depth point. */
export function buildBg(
  d: CoverSeedData,
  hueShift: number,
  mood: CoverMood,
): Rgb {
  const m = MOODS[mood];
  return oklch(
    clamp01(0.22 + m.lAdd * 0.4 + (mood === 'deep' ? -0.05 : 0)),
    0.05,
    d.baseHue + hueShift - 22,
  );
}
