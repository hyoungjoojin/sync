import { inferAdditionalFields } from 'better-auth/client/plugins';
import { createAuthClient } from 'better-auth/react';

import type { auth } from '.';

export const { useSession, signOut } = createAuthClient({
  // 서버 basePath와 반드시 일치해야 한다 (기본값 /api/auth 는 Spring이 소유).
  basePath: '/api/better-auth',
  plugins: [inferAdditionalFields<typeof auth>()],
});
