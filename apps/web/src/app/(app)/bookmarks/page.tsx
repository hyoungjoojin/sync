import { headers } from 'next/headers';
import { redirect } from 'next/navigation';

import { auth, isAuthenticated, isOnboarded } from '@/lib/auth';
import ROUTES from '@/util/routes';

import BookmarkedPosts from './_components/BookmarkedPosts';

export default async function BookmarksPage() {
  const session = await auth.api.getSession({
    headers: await headers(),
  });

  if (!isAuthenticated(session)) {
    redirect(ROUTES.LOGIN());
  }

  if (!isOnboarded(session)) {
    redirect(ROUTES.ONBOARDING());
  }

  return <BookmarkedPosts />;
}
