'use client';

import { useTranslations } from 'next-intl';

import { LinkButton } from '@/components/ui/button';
import ROUTES from '@/util/routes';

export const RESET_LINK_REASON = {
  EXPIRED: 'expired',
  INVALID: 'invalid',
} as const;

export type ResetLinkReason =
  (typeof RESET_LINK_REASON)[keyof typeof RESET_LINK_REASON];

interface InvalidResetLinkProps {
  reason: ResetLinkReason;
}

export default function InvalidResetLink({ reason }: InvalidResetLinkProps) {
  const t = useTranslations('pages.reset_password.invalid_link');

  const isExpired = reason === RESET_LINK_REASON.EXPIRED;

  const title = isExpired ? t('expired.title') : t('invalid.title');
  const description = isExpired
    ? t('expired.description')
    : t('invalid.description');

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h2 className="text-lg font-medium mb-2">{title}</h2>
        <p className="text-muted-foreground">{description}</p>
      </div>

      <LinkButton className="w-full" href={ROUTES.FORGOT_PASSWORD()}>
        {t('action.label')}
      </LinkButton>
    </div>
  );
}
