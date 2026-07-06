import { getTranslations } from 'next-intl/server';
import { headers } from 'next/headers';
import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { Copyright } from '@/components/ui/copyright';
import { Logo } from '@/components/ui/logo';
import { auth, isAuthenticated } from '@/lib/auth';
import ROUTES from '@/util/routes';

interface LegalLayoutProps {
  children: React.ReactNode;
}

export default async function LegalLayout({ children }: LegalLayoutProps) {
  const t = await getTranslations('pages.about');
  const session = await auth.api.getSession({ headers: await headers() });
  const loggedIn = isAuthenticated(session);

  return (
    <div className="w-full min-h-screen flex flex-col">
      <header className="flex items-center justify-between px-6 py-4 max-w-5xl w-full mx-auto">
        <Link href={ROUTES.HOME()}>
          <Logo />
        </Link>

        <div className="flex items-center gap-2">
          {loggedIn ? (
            <Button asChild>
              <Link href={ROUTES.HOME()}>{t('nav.app')}</Link>
            </Button>
          ) : (
            <>
              <Button variant="ghost" asChild>
                <Link href={ROUTES.LOGIN()}>{t('nav.login')}</Link>
              </Button>
              <Button asChild>
                <Link href={ROUTES.REGISTER()}>{t('nav.register')}</Link>
              </Button>
            </>
          )}
        </div>
      </header>

      <main className="flex-1">{children}</main>

      <footer className="px-6 py-12">
        <div className="max-w-5xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-8">
          <div className="col-span-2 md:col-span-1">
            <Logo />
            <p className="text-sm text-muted-foreground mt-3 max-w-[220px]">
              {t('footer.description')}
            </p>
          </div>

          <div>
            <h4 className="text-sm font-medium mb-3">
              {t('footer.legal.title')}
            </h4>
            <ul className="flex flex-col gap-2 text-sm text-muted-foreground">
              <li>
                <Link href={ROUTES.TERMS()} className="hover:text-foreground">
                  {t('footer.legal.terms')}
                </Link>
              </li>
              <li>
                <Link href={ROUTES.PRIVACY()} className="hover:text-foreground">
                  {t('footer.legal.privacy')}
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="max-w-5xl mx-auto mt-10 pt-6 border-t">
          <Copyright />
        </div>
      </footer>
    </div>
  );
}
