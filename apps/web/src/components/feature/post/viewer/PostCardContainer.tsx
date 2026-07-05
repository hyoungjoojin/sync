'use client';

import { useGetPostBySlug } from '@/api/__generated__/post/post';
import PostCard from '@/components/feature/post/viewer/PostCard';
import { Skeleton } from '@/components/ui/skeleton';

import { PostType } from '../types/post';

interface PostCardContainerProps {
  slug: string;
}

export default function PostCardContainer({ slug }: PostCardContainerProps) {
  const { data, isPending } = useGetPostBySlug(slug);

  if (isPending || !data) {
    return <Skeleton className="h-40 w-full" />;
  }

  const post = data.data;
  const { summary } = post;

  return (
    <PostCard
      id={summary.id}
      type={summary.type as PostType}
      author={summary.author}
      project={summary.project}
      content={post.content}
      likeCount={post.likeCount}
      commentCount={post.commentCount}
      bookmarked={post.bookmarked}
      createdAt={summary.createdAt}
    />
  );
}
