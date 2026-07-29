import { cn } from '@/lib/utils';

import { GeneratedPostCover } from './GeneratedPostCover';

export function PostCoverImage({
  url,
  seed,
  className,
}: {
  url?: string | null;
  seed: string;
  className?: string;
}) {
  if (!url) {
    return (
      <GeneratedPostCover
        seed={seed}
        className={cn('aspect-[2.5/1] w-full', className)}
      />
    );
  }

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={url}
      alt=""
      className={cn('aspect-[2.5/1] w-full object-cover', className)}
    />
  );
}
