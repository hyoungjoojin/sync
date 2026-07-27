import { NextResponse } from 'next/server';

import { env } from '@/lib/env';
import { DEFAULT_OG_IMAGE, toAbsoluteUrl } from '@/lib/seo';

// 커버 이미지는 만료되는 서명(presigned) URL이라 og:image에 직접 넣으면
// 크롤러가 재수집할 때 만료되어 미리보기가 깨진다.
// 이 라우트는 og:image가 가리킬 "불변" 경로를 제공하고, 요청 시점마다
// 최신 이미지 URL을 조회해 302 리다이렉트한다.
export const dynamic = 'force-dynamic';

export async function GET(
  _request: Request,
  { params }: { params: Promise<{ slug: string }> },
) {
  const { slug } = await params;
  const fallback = toAbsoluteUrl(DEFAULT_OG_IMAGE);

  let target = fallback;
  try {
    // 익명(비로그인) 컨텍스트로 조회 → 공개 게시물만 이미지가 노출된다.
    const response = await fetch(
      `${env.NEXT_PUBLIC_BACKEND_URL}/posts/${encodeURIComponent(slug)}`,
      { headers: { Accept: 'application/json' } },
    );

    if (response.ok) {
      const body = await response.json();
      const summary = body?.summary;
      target =
        summary?.coverImageUrl ?? summary?.previewMedia?.[0]?.url ?? fallback;
    }
  } catch {
    // 조회 실패 시 기본 이미지로 폴백한다.
  }

  const redirect = NextResponse.redirect(target, 302);
  // 서명 URL이 만료되기 전에 갱신되도록 짧게만 캐시한다.
  // (백엔드 서명 URL의 TTL보다 작게 유지할 것.)
  redirect.headers.set('Cache-Control', 'public, max-age=60, s-maxage=60');
  return redirect;
}
