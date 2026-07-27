import type { MetadataRoute } from 'next';

import { getSiteUrl } from '@/lib/seo';

const NON_PUBLIC_PATHS = [
  '/admin',
  '/auth',
  '/onboarding',
  '/bookmarks',
  '/collections',
  '/messages',
  '/search',
  '/posts/drafts',
  '/posts/my',
  '/posts/new',
  '/posts/*/edit',
  '/projects/new',
  '/projects/invitations',
  '/projects/join-requests',
  '/projects/*/settings',
  '/projects/*/collections',
  '/projects/*/tags/manage',
  '/projects/*/posts/bookmarks',
  '/projects/*/posts/drafts',
  '/projects/*/posts/my',
  '/projects/*/posts/my-comments',
  '/projects/*/posts/new',
  '/projects/*/posts/*/edit',
  // og:image 리다이렉트 라우트는 메타데이터 전용이라 색인 대상이 아니다.
  '/og/',
];

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: NON_PUBLIC_PATHS,
    },
    host: getSiteUrl().origin,
    sitemap: getSiteUrl('/sitemap.xml').toString(),
  };
}
