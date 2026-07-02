import { headers } from 'next/headers';
import { redirect } from 'next/navigation';

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { auth, isAuthenticated } from '@/lib/auth';
import ROUTES from '@/util/routes';

import UserProjects from './_components/UserProjects';

export default async function Projects() {
  const session = await auth.api.getSession({
    headers: await headers(),
  });

  if (!isAuthenticated(session)) {
    redirect(ROUTES.HOME());
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Your Workspaces</h1>
      </div>

      <Tabs defaultValue="member">
        <TabsList variant="line" className="border-b w-full justify-start">
          <TabsTrigger value="member">Member</TabsTrigger>
          <TabsTrigger value="following">Following</TabsTrigger>
          <TabsTrigger value="invitations">Invitations</TabsTrigger>
        </TabsList>

        <TabsContent value="member">
          <UserProjects />
        </TabsContent>
        <TabsContent value="following">
          {
            // TODO: 프로젝트 팔로잉 기능 구현
          }
        </TabsContent>
        <TabsContent value="invitations">
          {
            // TODO: 프로젝트 초대 기능 구현
          }
        </TabsContent>
      </Tabs>
    </div>
  );
}
