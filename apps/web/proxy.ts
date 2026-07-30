import { NextRequest, NextResponse } from 'next/server';

import ROUTES from '@/util/routes';

// Spring이 소유한 세션 쿠키(server.servlet.session.cookie.name=session)의
// 존재만 보는 빠른 경로다. 유효성 검증은 페이지의 requireOnboardedSession이 한다.
export function proxy(request: NextRequest) {
  if (!request.cookies.has('session')) {
    return NextResponse.redirect(new URL(ROUTES.ABOUT(), request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/'],
};
