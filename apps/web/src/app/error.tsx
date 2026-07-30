'use client';

import { useTranslations } from 'next-intl';
import Link from 'next/link';

import { Button, LinkButton } from '@/components/ui/button';
import { Logo } from '@/components/ui/logo';
import { ErrorCode, getErrorCodeFromDigest } from '@/lib/error';
import ROUTES from '@/util/routes';

interface ErrorPageProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function ErrorPage({ error, reset }: ErrorPageProps) {
  const t = useTranslations('pages.error');

  const isConnectionError =
    getErrorCodeFromDigest(error.digest) === ErrorCode.NETWORK_ERROR;

  return (
    <div className="flex min-h-screen w-full flex-col">
      <header className="mx-auto w-full max-w-5xl px-6 py-4">
        <Link href={ROUTES.HOME()}>
          <Logo />
        </Link>
      </header>

      <main className="flex flex-1 items-center justify-center px-6 py-12">
        <div className="flex w-full max-w-lg flex-col items-center text-center">
          <span className="font-mono text-xs tracking-widest text-muted-foreground uppercase">
            {t('eyebrow')}
          </span>

          <h1 className="mt-4 text-2xl font-semibold tracking-tight text-balance">
            {isConnectionError ? t('network-title') : t('title')}
          </h1>

          <p className="mt-2 text-sm text-muted-foreground text-balance">
            {isConnectionError ? t('network-description') : t('description')}
          </p>

          <div className="mt-8 flex items-center gap-3">
            <Button onClick={reset}>{t('retry')}</Button>
            <LinkButton href={ROUTES.HOME()} variant="outline">
              {t('back')}
            </LinkButton>
          </div>
        </div>
      </main>
    </div>
  );
}
