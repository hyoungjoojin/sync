import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

type ProfileAvatarProps = React.ComponentProps<typeof Avatar> & {
  name: string;
  imageUrl?: string | null;
};

function ProfileAvatar({
  name,
  imageUrl,
  size = 'default',
  ...props
}: ProfileAvatarProps) {
  return (
    <Avatar size={size} {...props}>
      <AvatarImage src={imageUrl || undefined} alt={name} />
      <AvatarFallback className="bg-primary font-semibold text-primary-foreground">
        {name.charAt(0).toUpperCase()}
      </AvatarFallback>
    </Avatar>
  );
}

export { ProfileAvatar };
