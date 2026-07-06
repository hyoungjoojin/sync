import { PlusIcon } from '@phosphor-icons/react';

import { LinkButton } from '@/components/ui/button';
import { Card } from '@/components/ui/card';

interface ProjectCardProps {
  name: string;
  handle: string;
}

function ProjectCard({ name, handle }: ProjectCardProps) {
  return (
    <Card className="justify-between gap-4 p-5">
      <div className="flex size-10 items-center justify-center rounded-lg bg-primary text-lg font-semibold text-primary-foreground">
        {name.charAt(0).toUpperCase()}
      </div>

      <div>
        <p className="font-semibold">{name}</p>
        <p className="text-muted-foreground text-sm">@{handle}</p>
      </div>

      <LinkButton href={`/projects/${handle}`} size="sm" className="w-full">
        Open
      </LinkButton>
    </Card>
  );
}

function NewProjectCard() {
  return (
    <Card className="items-center justify-center gap-3 border-dashed p-5 text-center">
      <div className="border-muted-foreground/40 flex size-10 items-center justify-center rounded-lg border border-dashed">
        <PlusIcon className="text-muted-foreground" />
      </div>
      <div>
        <p className="font-semibold">New workspace</p>
        <p className="text-muted-foreground text-sm">
          For a team, company, or open-source project.
        </p>
      </div>
      <LinkButton href="/projects/new" size="sm" className="w-full">
        Create
      </LinkButton>
    </Card>
  );
}

export { ProjectCard, NewProjectCard };
