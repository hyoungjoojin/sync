import { useTranslations } from 'next-intl';

import ForgotPasswordForm from './_components/ForgotPasswordForm';

export default function ForgotPassword() {
  const t = useTranslations('pages.forgot_password');

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-2xl font-light mb-2">{t('title')}</h1>
        <p className="text-muted-foreground">{t('description')}</p>
      </div>

      <ForgotPasswordForm />
    </div>
  );
}
