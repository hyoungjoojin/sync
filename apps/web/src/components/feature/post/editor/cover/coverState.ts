import type { CoverParams } from './generators';

/**
 * Editor-facing cover state.
 *
 * - `none`      — no cover (either never set, or the author removed one).
 * - `existing`  — the loaded post already had a cover; unchanged so far.
 * - `generated` — a locally-rendered cover the author picked from the gallery.
 *   It is NOT uploaded yet: only its deterministic `params` and a local
 *   `previewUrl` are held. The image is rendered and uploaded lazily when the
 *   post is saved (see `PostEditor`), so picking a cover never touches the
 *   network.
 * - `uploaded`  — a file the author chose from their device. Like `generated`,
 *   it is held locally and only uploaded when the post is saved.
 */
export type CoverState =
  | { kind: 'none' }
  | { kind: 'existing'; url: string }
  | { kind: 'generated'; params: CoverParams; previewUrl: string }
  | { kind: 'uploaded'; file: File; previewUrl: string };

export function initialCoverState(coverImageUrl?: string | null): CoverState {
  return coverImageUrl
    ? { kind: 'existing', url: coverImageUrl }
    : { kind: 'none' };
}

/** The URL to show in the cover slot preview, if any. */
export function coverPreviewUrl(state: CoverState): string | null {
  if (state.kind === 'existing') return state.url;
  if (state.kind === 'generated' || state.kind === 'uploaded')
    return state.previewUrl;
  return null;
}
