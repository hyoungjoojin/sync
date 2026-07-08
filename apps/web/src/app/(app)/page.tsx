import { redirect } from 'next/navigation';

import { requireOnboardedSession } from '@/lib/auth/guards';
import ROUTES from '@/util/routes';

export default async function Home() {
  await requireOnboardedSession();

  redirect(ROUTES.EXPLORE_FOLLOWING());
}
