import { useTranslations } from 'next-intl';
import type { ReactNode } from 'react';

import { env } from '@/lib/env';

export default function CaptchaNotice() {
  const t = useTranslations('pages.register.form');

  if (!env.NEXT_PUBLIC_CAPTCHA_SITE_KEY) {
    return null;
  }

  return (
    <p className="text-center text-xs text-muted-foreground">
      {t.rich('captcha_notice', {
        privacy: (chunks: ReactNode) => (
          <a
            href="https://policies.google.com/privacy"
            target="_blank"
            rel="noopener noreferrer"
            className="underline"
          >
            {chunks}
          </a>
        ),
        terms: (chunks: ReactNode) => (
          <a
            href="https://policies.google.com/terms"
            target="_blank"
            rel="noopener noreferrer"
            className="underline"
          >
            {chunks}
          </a>
        ),
      })}
    </p>
  );
}
