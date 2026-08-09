/**
 * Generative cover-image types.
 *
 * A cover is produced entirely in the browser and is deterministic from its
 * `seed`: identical `CoverParams` always render a visually identical PNG. Only
 * the rendered image is persisted (as a media id) — these params stay
 * editor-local and are not stored on the server.
 */

export type CoverAlgo = 'aurora' | 'mesh' | 'flow';

export type CoverMood = 'soft' | 'vivid' | 'muted' | 'deep';

export interface CoverParams {
  algo: CoverAlgo;
  /** Deterministic seed. Same seed + params ⇒ same image. */
  seed: string;
  mood: CoverMood;
  /** Hue rotation in degrees, -180..180. */
  hueShift: number;
  /** Pattern density / detail, 0..1. */
  complexity: number;
  /** Film-grain amount, 0..0.2. */
  grain: number;
  /** Edge darkening, 0..0.7. */
  vignette: number;
}

/** Linear-ordered sRGB channels in 0..1. */
export type Rgb = readonly [number, number, number];

/** The four-stop signature palette a seed produces. */
export type Palette = readonly [Rgb, Rgb, Rgb, Rgb];

export type Vec2 = readonly [number, number];

/** Canonical cover render resolution (2.5:1). */
export const COVER_WIDTH = 1500;
export const COVER_HEIGHT = 600;

export const COVER_ALGOS: readonly CoverAlgo[] = ['aurora', 'mesh', 'flow'];
export const COVER_MOODS: readonly CoverMood[] = [
  'soft',
  'vivid',
  'muted',
  'deep',
];

export const DEFAULT_COVER_PARAMS: Omit<CoverParams, 'seed'> = {
  algo: 'aurora',
  mood: 'soft',
  hueShift: 0,
  complexity: 0.45,
  grain: 0.07,
  vignette: 0.28,
};
