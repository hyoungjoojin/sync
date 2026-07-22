/**
 * Deterministic randomization of the high-level cover params.
 *
 * The generators already vary their *pattern* with `seed`; this rolls the
 * color/mood/texture knobs too, each within a low/high bound, so every re-roll
 * produces a genuinely different cover instead of the same look with a new
 * pattern. It is a pure function of `seed`, so re-rendering the stored params
 * on save reproduces the exact same image.
 */
import { mulberry32, strHash } from './rng';
import { COVER_ALGOS, COVER_MOODS, type CoverParams } from './types';

/** Inclusive [low, high] bounds each rolled param is sampled from. */
export const COVER_PARAM_BOUNDS = {
  /** Hue rotation in degrees. */
  hueShift: [-180, 180],
  /** Pattern density / detail. */
  complexity: [0.3, 0.75],
  /** Film-grain amount. */
  grain: [0.03, 0.12],
  /** Edge darkening. */
  vignette: [0.15, 0.4],
} as const;

/**
 * Roll a full set of cover params from `seed`. `algo` and `mood` are picked
 * from their fixed sets; the numeric knobs are sampled within
 * {@link COVER_PARAM_BOUNDS}. Salted so this stream is independent of the
 * palette's own use of `seed`.
 */
export function randomCoverParams(seed: string): CoverParams {
  const rand = mulberry32(strHash(`params:${seed}`));
  const pick = <T>(arr: readonly T[]): T =>
    arr[Math.floor(rand() * arr.length)] as T;
  const between = ([lo, hi]: readonly [number, number]) =>
    lo + rand() * (hi - lo);
  const b = COVER_PARAM_BOUNDS;

  return {
    algo: pick(COVER_ALGOS),
    seed,
    mood: pick(COVER_MOODS),
    hueShift: between(b.hueShift),
    complexity: between(b.complexity),
    grain: between(b.grain),
    vignette: between(b.vignette),
  };
}
