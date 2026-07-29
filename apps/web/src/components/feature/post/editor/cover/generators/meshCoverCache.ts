/**
 * 피드에서 즉석으로 만들어 쓰는 mesh 커버 캐시.
 *
 * 브라우저는 살아 있는 WebGL 컨텍스트를 16개 남짓으로 제한하므로 카드마다
 * 캔버스를 두면 피드가 금방 한도를 넘긴다. 그래서 캔버스와 렌더러를 모듈에
 * 하나만 두고 씨앗별로 그린 결과를 data URL 로 재사용한다.
 *
 * 브라우저 전용 — 서버에서 호출하면 안 된다.
 */
import { randomCoverParams } from './randomize';
import { COVER_HEIGHT, COVER_WIDTH } from './types';
import { type GLCoverRenderer, createGLCoverRenderer } from './webgl';

/** 피드 카드용이라 저장용 해상도의 절반이면 충분하다. */
const SCALE = 0.5;

/** data URL 은 base64라 무겁다. 무한 스크롤에서 늘어나지 않게 상한을 둔다. */
const MAX_CACHED = 40;

const cache = new Map<string, string>();

let canvas: HTMLCanvasElement | null = null;
let renderer: GLCoverRenderer | null = null;
let unavailable = false;

/**
 * `seed` 의 mesh 커버를 그려 data URL 로 돌려준다. 같은 씨앗은 캐시된 같은
 * 문자열을 그대로 돌려주므로 렌더 중에 호출해도 안전하다. WebGL 을 쓸 수 없는
 * 환경에서는 `null`.
 */
export function getMeshCoverDataUrl(seed: string): string | null {
  const cached = cache.get(seed);
  if (cached !== undefined) {
    return cached;
  }
  if (unavailable) {
    return null;
  }

  try {
    if (!canvas || !renderer) {
      canvas = document.createElement('canvas');
      canvas.width = COVER_WIDTH * SCALE;
      canvas.height = COVER_HEIGHT * SCALE;
      renderer = createGLCoverRenderer(canvas);
    }

    renderer.render({ ...randomCoverParams(seed), algo: 'mesh' });
    const dataUrl = canvas.toDataURL('image/png');

    if (cache.size >= MAX_CACHED) {
      const oldest = cache.keys().next();
      if (!oldest.done) {
        cache.delete(oldest.value);
      }
    }
    cache.set(seed, dataUrl);

    return dataUrl;
  } catch {
    // WebGL 이 없거나 컨텍스트를 잃은 경우. 커버는 장식이라 조용히 포기한다.
    unavailable = true;
    return null;
  }
}
