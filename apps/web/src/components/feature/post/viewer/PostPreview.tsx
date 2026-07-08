import {
  type PostStatus,
  type PostType,
} from '@/components/feature/post/types/post';

import PostViewCard, { type PostViewCardProps } from './PostViewCard';
import { type PostAuthorSummary, type PostProjectSummary } from './types';

export type PostPreviewProps = Omit<PostViewCardProps, 'variant'>;

export default function PostPreview(props: PostPreviewProps) {
  return <PostViewCard {...props} variant="preview" />;
}

interface PostSummaryLike {
  id: number;
  slug: string;
  type: string;
  status?: string;
  title?: string | null;
  author: PostAuthorSummary;
  project?: PostProjectSummary;
  liked: boolean;
  likeCount: number;
  commentCount: number;
  bookmarked: boolean;
  isAuthor: boolean;
  createdAt: string;
}

interface PostContentLike {
  summary: PostSummaryLike;
  content: string;
}

export function toPostPreviewProps(post: PostContentLike): PostPreviewProps {
  const { summary } = post;

  return {
    id: summary.id,
    slug: summary.slug,
    type: summary.type as PostType,
    status: summary.status as PostStatus | undefined,
    title: summary.title,
    author: summary.author,
    project: summary.project,
    content: { json: post.content, media: [] },
    liked: summary.liked,
    likeCount: summary.likeCount,
    commentCount: summary.commentCount,
    bookmarked: summary.bookmarked,
    isAuthor: summary.isAuthor,
    createdAt: summary.createdAt,
  };
}
