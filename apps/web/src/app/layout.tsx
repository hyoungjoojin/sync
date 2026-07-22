import type { Metadata } from 'next';
import { JetBrains_Mono } from 'next/font/google';
import localFont from 'next/font/local';

import AppProvider from '@/components/providers/AppProvider';
import { Toaster } from '@/components/ui/sonner';
import { siteMetadataBase } from '@/lib/seo';
import '@/styles/globals.css';

const pretendard = localFont({
  src: '../../public/fonts/PretendardVariable.woff2',
  variable: '--font-sans',
  display: 'swap',
  weight: '45 920',
});

const jetbrainsMono = JetBrains_Mono({
  variable: '--font-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  metadataBase: siteMetadataBase,
  title: {
    default: 'sync',
    template: '%s | sync',
  },
  description: '개발자들이 지식과 경험을 기록하고 함께 나누는 공간, sync.',
  openGraph: {
    type: 'website',
    siteName: 'sync',
    title: 'sync',
    description: '개발자들이 지식과 경험을 기록하고 함께 나누는 공간, sync.',
    images: ['/og-default.png'],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'sync',
    description: '개발자들이 지식과 경험을 기록하고 함께 나누는 공간, sync.',
    images: ['/og-default.png'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning className={pretendard.variable}>
      <body className={`${jetbrainsMono.variable} antialiased`}>
        <AppProvider>{children}</AppProvider>
        <Toaster />
      </body>
    </html>
  );
}
