'use client';

import { useGetPostBySlug } from '@/api/__generated__/post/post';
import PostCard from '@/components/feature/post/viewer/PostCard';
import { Skeleton } from '@/components/ui/skeleton';

import { PostScope, PostStatus, PostType } from '../types/post';

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
      slug={slug}
      type={summary.type as PostType}
      scope={summary.scope as PostScope}
      status={summary.status as PostStatus}
      title={summary.title}
      author={summary.author}
      project={summary.project}
      content={post.content}
      likeCount={summary.likeCount}
      commentCount={summary.commentCount}
      bookmarked={post.bookmarked}
      isAuthor={summary.isAuthor}
      createdAt={summary.createdAt}
    />
  );
}
