import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';

type ProjectAvatarProps = React.ComponentProps<typeof Avatar> & {
  name: string;
  iconUrl?: string | null;
};

function ProjectAvatar({
  name,
  iconUrl,
  size = 'default',
  className,
  ...props
}: ProjectAvatarProps) {
  return (
    <Avatar size={size} className={cn('rounded-lg', className)} {...props}>
      <AvatarImage src={iconUrl || undefined} alt={name} />
      <AvatarFallback className="rounded-lg bg-primary font-semibold text-primary-foreground">
        {name.charAt(0).toUpperCase()}
      </AvatarFallback>
    </Avatar>
  );
}

export { ProjectAvatar };
