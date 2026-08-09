import { getTranslations } from 'next-intl/server';

import InvalidResetLink, {
  RESET_LINK_REASON,
} from './_components/InvalidResetLink';
import ResetPasswordForm from './_components/ResetPasswordForm';

interface ResetPasswordProps {
  searchParams: Promise<{ token?: string }>;
}

export default async function ResetPassword({
  searchParams,
}: ResetPasswordProps) {
  const t = await getTranslations('pages.reset_password');
  const { token } = await searchParams;

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-2xl font-light mb-2">{t('title')}</h1>
        <p className="text-muted-foreground">{t('description')}</p>
      </div>

      {token ? (
        <ResetPasswordForm token={token} />
      ) : (
        <InvalidResetLink reason={RESET_LINK_REASON.INVALID} />
      )}
    </div>
  );
}
