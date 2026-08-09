import { getTranslations } from 'next-intl/server';

import { SeparatorWithText } from '@/components/ui/separator';

import AuthTermsNotice from '../_components/AuthTermsNotice';
import OAuthProviders from '../_components/OAuthProviders';
import LoginForm from './_components/LoginForm';

interface LoginProps {
  searchParams: Promise<{ redirect?: string }>;
}

/**
 * 같은 오리진의 절대 경로만 통과시킨다. `//host` 와 `/\host` 는 브라우저가 다른 오리진으로 읽으므로
 * 여기서 걸러 내지 않으면 로그인 화면이 오픈 리다이렉트가 된다.
 */
function safeRedirect(redirect: string | undefined) {
  if (!redirect || !redirect.startsWith('/')) return undefined;
  if (redirect.startsWith('//') || redirect.startsWith('/\\')) return undefined;

  return redirect;
}

export default async function Login({ searchParams }: LoginProps) {
  const t = await getTranslations('pages.login');
  const tAuth = await getTranslations('pages.auth');
  const { redirect } = await searchParams;

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-2xl font-light mb-2">{t('title')}</h1>
        <p className="text-muted-foreground">{t('description')}</p>
      </div>

      <div className="flex flex-col gap-6">
        <LoginForm redirectTo={safeRedirect(redirect)} />

        <SeparatorWithText>{tAuth('or')}</SeparatorWithText>

        <OAuthProviders />

        <AuthTermsNotice />
      </div>
    </div>
  );
}
