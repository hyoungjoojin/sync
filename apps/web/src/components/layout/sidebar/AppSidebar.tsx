'use client';

import { usePathname } from 'next/navigation';

import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import { Copyright } from '@/components/ui/copyright';
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuSkeleton,
} from '@/components/ui/sidebar';
import { Skeleton } from '@/components/ui/skeleton';
import { useMounted } from '@/hooks/use-mounted';
import { useProjectContextHandle } from '@/hooks/use-project-context';
import { useSession } from '@/lib/auth/client';
import { isAuthenticated } from '@/lib/auth/utils';

import AdminSidebarContent from './AdminSidebarContent';
import PersonalSidebarContent from './PersonalSidebarContent';
import ProjectSidebarContent from './ProjectSidebarContent';

export default function AppSidebar() {
  const pathname = usePathname();
  const isAdmin = pathname.startsWith('/admin');
  const handle = useProjectContextHandle();

  // Same `mounted` gating used elsewhere for session-dependent rendering
  // (e.g. `PersonalSidebarContent`, `TopNavigationBar`) — SSR always renders
  // the logged-out state, so this avoids a hydration mismatch once the
  // session resolves client-side.
  const mounted = useMounted();
  const { data: session, isPending: isSessionPending } = useSession();

  const { isPending, isError } = useGetProjectByHandle(handle ?? '', {
    query: { enabled: !!handle && !isAdmin },
  });

  if (!mounted || isSessionPending || !isAuthenticated(session)) {
    return null;
  }

  return (
    <Sidebar>
      {isAdmin ? (
        <AdminSidebarContent />
      ) : !handle || isError ? (
        <PersonalSidebarContent />
      ) : isPending ? (
        <ProjectSidebarSkeleton />
      ) : (
        <ProjectSidebarContent handle={handle} />
      )}

      <div className="mb-4 px-3">
        <Copyright className="text-sidebar-foreground/40" />
      </div>
    </Sidebar>
  );
}

function ProjectSidebarSkeleton() {
  return (
    <>
      <SidebarHeader className="flex flex-col gap-3 p-4">
        <Skeleton className="h-3 w-16" />

        <div className="flex items-center gap-2">
          <Skeleton className="size-8 shrink-0 rounded-lg" />
          <Skeleton className="h-4 w-24" />
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {Array.from({ length: 3 }).map((_, index) => (
                <SidebarMenuItem key={index}>
                  <SidebarMenuSkeleton showIcon />
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </>
  );
}
