import { cn } from '@/lib/utils';

export interface PostPreviewBodyProps {
  preview: string;
  className?: string;
}

export function PostPreviewBody({ preview, className }: PostPreviewBodyProps) {
  return (
    <p
      className={cn(
        'text-muted-foreground text-[0.9375rem] leading-relaxed',
        className,
      )}
    >
      {preview}
    </p>
  );
}
