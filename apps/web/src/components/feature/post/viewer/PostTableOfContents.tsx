'use client';

import { usePostContext } from './PostContext';
import { PostTableOfContentsList } from './components/PostTableOfContentsList';

export function PostTableOfContents() {
  const { toc } = usePostContext();

  return <PostTableOfContentsList items={toc} />;
}
