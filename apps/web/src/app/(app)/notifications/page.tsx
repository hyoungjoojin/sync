import { requireOnboardedSession } from '@/lib/auth/guards';

import NotificationsList from './_components/NotificationsList';

export default async function NotificationsPage() {
  await requireOnboardedSession();

  return <NotificationsList />;
}
