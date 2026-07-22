import type { GetCollectionPostsResponsePostsNodesItemContent } from '@/api/__generated__/types/GetCollectionPostsResponsePostsNodesItemContent';
import type { GetCollectionPostsResponsePostsNodesItemContentPost } from '@/api/__generated__/types/GetCollectionPostsResponsePostsNodesItemContentPost';
import {
  type PostSummary,
  toPostSummary,
} from '@/components/feature/post/viewer/types';

export const COLLECTION_POSTS_PAGE_SIZE = '30';

/**
 * 컬렉션 "목록" 쿼리(개인 `/users/{id}/collections`, 프로젝트
 * `/projects/{handle}/collections`)의 키를 식별한다. postHandle 파라미터 유무로
 * 키 변형이 생기므로, 목록 캐시를 직접 수정하는 훅들은 이 프레디킷으로 두 스코프의
 * 모든 변형을 한 번에 갱신한다. (상세 `/collections/{id}` 나 항목 목록
 * `/collections/{id}/posts` 는 `/collections` 로 끝나지 않아 매칭되지 않는다.)
 */
export function isCollectionsListKey(queryKey: readonly unknown[]): boolean {
  return (
    typeof queryKey[0] === 'string' && queryKey[0].endsWith('/collections')
  );
}

export interface CollectionItem {
  collectionPostId: number;
  /**
   * 뷰어가 열람 가능한 경우에만 존재한다. 없으면 삭제·비공개로 열람 불가한
   * tombstone 이다.
   */
  summary?: PostSummary;
}

/**
 * 컬렉션 항목의 게시글(nullable 필드로 생성된 DTO)을 앱의 PostSummary 로
 * 변환한다. VISIBLE 항목은 서버가 모든 필드를 채워 보내지만 생성 타입이
 * 전부 optional 이라 기본값으로 좁혀 준다.
 */
function toSummary(
  post: GetCollectionPostsResponsePostsNodesItemContentPost,
): PostSummary {
  return toPostSummary({
    id: post.id ?? 0,
    slug: post.slug ?? '',
    type: post.type ?? 'SHORT',
    status: post.status ?? 'PUBLISHED',
    scope: post.scope ?? 'PUBLIC',
    title: post.title ?? null,
    author: {
      name: post.author?.name ?? '',
      handle: post.author?.handle ?? '',
      profileImageUrl: post.author?.profileImageUrl ?? null,
    },
    project: post.project
      ? { handle: post.project.handle, name: post.project.name }
      : undefined,
    liked: post.liked ?? false,
    likeCount: post.likeCount ?? 0,
    bookmarked: post.bookmarked ?? false,
    commentCount: post.commentCount ?? 0,
    isAuthor: post.isAuthor ?? false,
    createdAt: post.createdAt ?? '',
    resolved: post.resolved ?? false,
    tags: (post.tags ?? []).map((tag) => ({
      id: tag.id ?? 0,
      name: tag.name ?? '',
      description: tag.description ?? null,
      postCount: tag.postCount ?? 0,
      followerCount: tag.followerCount ?? 0,
      projectHandle: tag.projectHandle ?? null,
      isFollowing: tag.isFollowing ?? false,
    })),
    preview: post.preview ?? '',
    wordCount: post.wordCount ?? 0,
    previewMedia: (post.previewMedia ?? []).map((media) => ({
      id: media.id ?? 0,
      url: media.url ?? '',
    })),
    mediaCount: post.mediaCount ?? 0,
    coverImageUrl: post.coverImageUrl ?? null,
  });
}

export function toCollectionItem(
  content: GetCollectionPostsResponsePostsNodesItemContent,
): CollectionItem {
  return {
    collectionPostId: content.collectionPostId,
    summary: content.post ? toSummary(content.post) : undefined,
  };
}
