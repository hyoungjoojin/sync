import Link from 'next/link';

import ROUTES from '@/util/routes';

import type { PostTagSummary } from '../types';

export function PostTags({ tags }: { tags: PostTagSummary[] }) {
  if (tags.length === 0) {
    return null;
  }

  return (
    <div className="flex flex-wrap gap-x-3 gap-y-1 text-sm">
      {tags.map((tag) => (
        <Link
          key={tag.id}
          href={
            tag.projectHandle
              ? ROUTES.PROJECT_TAG(tag.projectHandle, String(tag.id))
              : ROUTES.TAG(String(tag.id))
          }
          onClick={(event) => event.stopPropagation()}
          className="text-muted-foreground hover:text-foreground transition-colors"
        >
          {`#${tag.name}`}
        </Link>
      ))}
    </div>
  );
}
