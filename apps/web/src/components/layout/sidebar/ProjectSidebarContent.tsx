'use client';

import {
  BookOpenIcon,
  CaretDownIcon,
  GearIcon,
  HouseIcon,
  PencilIcon,
  QuestionIcon,
  RssIcon,
  TagIcon,
} from '@phosphor-icons/react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';

import {
  useGetProjectByHandle,
  useSearchMyProjects,
} from '@/api/__generated__/project/project';
import { ProjectAvatar } from '@/components/feature/project/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
} from '@/components/ui/sidebar';
import { useRequireAuth } from '@/hooks/use-require-auth';
import ROUTES from '@/util/routes';

import SidebarCloseButton from './SidebarCloseButton';

interface ProjectSidebarContentProps {
  handle: string;
}

export default function ProjectSidebarContent({
  handle,
}: ProjectSidebarContentProps) {
  const { isAuthenticated } = useRequireAuth();

  return (
    <>
      <SidebarHeader>
        <div className="flex items-center justify-end">
          <SidebarCloseButton />
        </div>

        <ProjectSwitcher handle={handle} />

        <AskOrWriteButton handle={handle} />
      </SidebarHeader>

      <SidebarContent>
        <Browse handle={handle} />

        {isAuthenticated && (
          <>
            <SidebarSeparator />
            <Settings handle={handle} />
          </>
        )}
      </SidebarContent>
    </>
  );
}

interface SectionProps {
  handle: string;
}

function ProjectSwitcher({ handle }: SectionProps) {
  const [isProjectMenuOpen, setIsProjectMenuOpen] = useState(false);
  const { isAuthenticated } = useRequireAuth();
  const { data } = useGetProjectByHandle(handle);
  const { data: myProjectsData } = useSearchMyProjects(
    { query: '' },
    { query: { enabled: isAuthenticated } },
  );

  const projectName = data?.data.summary.name ?? handle;
  const projectIconUrl = data?.data.summary.iconUrl;
  const myProjects = myProjectsData?.data.projects ?? [];

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu
          open={isProjectMenuOpen}
          onOpenChange={setIsProjectMenuOpen}
        >
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton
              size="lg"
              className="rounded-lg border border-sidebar-border"
              data-state={isProjectMenuOpen ? 'open' : 'closed'}
            >
              <ProjectAvatar name={projectName} iconUrl={projectIconUrl} />
              <span className="truncate font-medium">{projectName}</span>
              <CaretDownIcon
                className={`ml-auto transition-transform ${
                  isProjectMenuOpen ? 'rotate-180' : ''
                }`}
              />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-64">
            <DropdownMenuLabel>Your Projects</DropdownMenuLabel>
            {myProjects.map((project) => (
              <DropdownMenuItem key={project.handle} asChild>
                <Link href={ROUTES.PROJECT(project.handle)}>
                  <ProjectAvatar
                    name={project.name}
                    iconUrl={project.iconUrl}
                    size="sm"
                  />
                  <span className="truncate">{project.name}</span>
                </Link>
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  );
}

function AskOrWriteButton({ handle }: SectionProps) {
  const pathname = usePathname();
  const { requireAuth } = useRequireAuth();

  return (
    <SidebarMenu>
      <SidebarMenuButton
        asChild
        isActive={pathname === ROUTES.NEW_PROJECT_POST(handle)}
        className="bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary data-[active=true]:bg-primary/20 data-[active=true]:text-primary"
      >
        <Link
          href={ROUTES.NEW_PROJECT_POST(handle)}
          onClick={(event) => {
            if (
              !requireAuth({
                intent: 'write',
                redirectTo: ROUTES.NEW_PROJECT_POST(handle),
              })
            ) {
              event.preventDefault();
            }
          }}
        >
          <PencilIcon />
          Ask / Write
        </Link>
      </SidebarMenuButton>
    </SidebarMenu>
  );
}

function Browse({ handle }: SectionProps) {
  const pathname = usePathname();

  const items = [
    {
      label: 'Home',
      href: ROUTES.PROJECT(handle),
      icon: HouseIcon,
      isActive: pathname === ROUTES.PROJECT(handle),
    },
    {
      label: 'Feed',
      href: ROUTES.PROJECT_POSTS(handle),
      icon: RssIcon,
      isActive: pathname === ROUTES.PROJECT_POSTS(handle),
    },
    {
      label: 'Questions',
      href: ROUTES.PROJECT(handle),
      icon: QuestionIcon,
      isActive: false,
    },
    {
      label: 'Guides',
      href: ROUTES.PROJECT(handle),
      icon: BookOpenIcon,
      isActive: false,
    },
    {
      label: 'Tags',
      href: ROUTES.PROJECT_TAGS(handle),
      icon: TagIcon,
      isActive: pathname === ROUTES.PROJECT_TAGS(handle),
    },
  ];

  return (
    <SidebarGroup>
      <SidebarGroupLabel>Browse</SidebarGroupLabel>
      <SidebarGroupContent>
        <SidebarMenu>
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <SidebarMenuItem key={item.label}>
                <SidebarMenuButton asChild isActive={item.isActive}>
                  <Link href={item.href}>
                    <Icon />
                    {item.label}
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            );
          })}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

function Settings({ handle }: SectionProps) {
  const pathname = usePathname();

  return (
    <SidebarGroup>
      <SidebarGroupContent>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              asChild
              isActive={pathname.startsWith(ROUTES.PROJECT_SETTINGS(handle))}
            >
              <Link href={ROUTES.PROJECT_SETTINGS(handle)}>
                <GearIcon />
                Settings
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}
