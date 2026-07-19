import type { NextConfig } from 'next';
import createNextIntlPlugin from 'next-intl/plugin';

const nextConfig: NextConfig = {
  output: 'standalone',
  rewrites: async () => {
    return [
      {
        source: '/@:handle/:path*',
        destination: '/profile/:handle/:path*',
      },
    ];
  },
  images: {
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '4566',
        pathname: '/**',
      },
      {
        protocol: 'https',
        hostname: 'skkil-sync-media.s3.ap-northeast-2.amazonaws.com',
        pathname: '/**',
      },
    ],
  },
};

const withNextIntl = createNextIntlPlugin('./src/lib/i18n.ts');
export default withNextIntl(nextConfig);
