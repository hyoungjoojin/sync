import type { QueryClient, QueryKey } from '@tanstack/react-query';

import {
  BOOKMARKED_POSTS_PATH,
  LIKED_POSTS_PATH,
  getQueryPath,
  isPostRelatedQueryKey,
} from './postQueryKeys';

interface CachedPostSummary {
  id: number;
  liked: boolean;
  likeCount: number;
  bookmarked: boolean;
  commentCount?: number | null;
}

/**
 * 바꿀 것이 없으면 `null` 을 돌려준다. 낙관적 갱신(`onMutate`)과 성공 확정
 * (`onSuccess`)이 같은 패치를 두 번 적용하므로, 이미 반영된 요약은 건드리지
 * 않아야 좋아요 수가 두 번 오르지 않는다.
 */
type SummaryPatch = (
  summary: CachedPostSummary,
) => Partial<CachedPostSummary> | null;

export type PostCacheSnapshot = [QueryKey, unknown][];

function isPlainObject(value: unknown): value is Record<string, unknown> {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const prototype: unknown = Object.getPrototypeOf(value);

  return prototype === Object.prototype || prototype === null;
}

/**
 * 게시물 요약이 응답 어디에 들어 있는지는 엔드포인트마다 다르다
 * (`posts[]`, `posts.nodes[].content`, `summary`, …). 모양을 엔드포인트별로
 * 나열하는 대신 `id` + 좋아요/북마크 필드 조합으로 요약 객체를 식별한다.
 */
function isPostSummaryOf(
  value: unknown,
  postId: number,
): value is CachedPostSummary {
  return (
    isPlainObject(value) &&
    value.id === postId &&
    typeof value.liked === 'boolean' &&
    typeof value.likeCount === 'number' &&
    typeof value.bookmarked === 'boolean'
  );
}

function wrapsPostSummaryOf(value: unknown, postId: number) {
  return (
    isPostSummaryOf(value, postId) ||
    (isPlainObject(value) && isPostSummaryOf(value.content, postId))
  );
}

/**
 * 바뀐 곳이 없는 가지는 원래 참조를 그대로 돌려준다. 이미지 URL 이 요청마다
 * 새로 서명되므로, 목록을 다시 불러오지 않고 참조도 유지해야 다른 카드가
 * 다시 렌더되며 이미지가 깜빡이는 것을 막을 수 있다.
 */
function patchNode(
  node: unknown,
  postId: number,
  patch: SummaryPatch,
): unknown {
  if (isPostSummaryOf(node, postId)) {
    const patched = patch(node);

    return patched ? { ...node, ...patched } : node;
  }

  if (Array.isArray(node)) {
    let changed = false;
    const next = node.map((item) => {
      const patched = patchNode(item, postId, patch);
      changed ||= patched !== item;

      return patched;
    });

    return changed ? next : node;
  }

  if (isPlainObject(node)) {
    let changed = false;
    const next: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(node)) {
      const patched = patchNode(value, postId, patch);
      changed ||= patched !== value;
      next[key] = patched;
    }

    return changed ? next : node;
  }

  return node;
}

function removeNode(node: unknown, postId: number): unknown {
  if (Array.isArray(node)) {
    const kept = node.filter((item) => !wrapsPostSummaryOf(item, postId));
    let changed = kept.length !== node.length;
    const next = kept.map((item) => {
      const removed = removeNode(item, postId);
      changed ||= removed !== item;

      return removed;
    });

    return changed ? next : node;
  }

  if (isPlainObject(node)) {
    let changed = false;
    const next: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(node)) {
      const removed = removeNode(value, postId);
      changed ||= removed !== value;
      next[key] = removed;
    }

    return changed ? next : node;
  }

  return node;
}

function patchCachedPost(
  queryClient: QueryClient,
  postId: number,
  patch: SummaryPatch,
) {
  queryClient.setQueriesData<unknown>(
    { predicate: (query) => isPostRelatedQueryKey(query.queryKey) },
    (previous: unknown) => patchNode(previous, postId, patch),
  );
}

function removeFromCachedList(
  queryClient: QueryClient,
  postId: number,
  path: string,
) {
  queryClient.setQueriesData<unknown>(
    { predicate: (query) => getQueryPath(query.queryKey) === path },
    (previous: unknown) => removeNode(previous, postId),
  );
}

/**
 * 목록에 새로 들어가야 하는 경우에는 정렬 위치를 알 수 없으므로 stale 로만
 * 표시한다. `refetchType: 'none'` 이라 지금 다시 불러오지 않고, 다음에 그
 * 목록을 열 때 서버 순서대로 채워진다.
 */
function markListStale(queryClient: QueryClient, path: string) {
  return queryClient.invalidateQueries({
    predicate: (query) => getQueryPath(query.queryKey) === path,
    refetchType: 'none',
  });
}

/**
 * 낙관적 갱신을 되돌릴 수 있게 게시물 관련 쿼리의 현재 데이터를 담아 둔다.
 * 캐시 데이터는 불변으로 다루므로 참조만 들고 있으면 된다.
 */
export function snapshotPostCache(queryClient: QueryClient): PostCacheSnapshot {
  return queryClient.getQueriesData<unknown>({
    predicate: (query) => isPostRelatedQueryKey(query.queryKey),
  });
}

export function restorePostCache(
  queryClient: QueryClient,
  snapshot: PostCacheSnapshot,
) {
  for (const [queryKey, data] of snapshot) {
    queryClient.setQueryData(queryKey, data);
  }
}

export function applyBookmarkToCache(
  queryClient: QueryClient,
  postId: number,
  bookmarked: boolean,
) {
  patchCachedPost(queryClient, postId, (summary) =>
    summary.bookmarked === bookmarked ? null : { bookmarked },
  );

  if (bookmarked) {
    markListStale(queryClient, BOOKMARKED_POSTS_PATH);
  } else {
    removeFromCachedList(queryClient, postId, BOOKMARKED_POSTS_PATH);
  }
}

export function applyCommentCountToCache(
  queryClient: QueryClient,
  postId: number,
  delta: number,
) {
  patchCachedPost(queryClient, postId, (summary) =>
    typeof summary.commentCount === 'number'
      ? { commentCount: Math.max(0, summary.commentCount + delta) }
      : null,
  );
}

export function applyLikeToCache(
  queryClient: QueryClient,
  postId: number,
  liked: boolean,
) {
  patchCachedPost(queryClient, postId, (summary) =>
    summary.liked === liked
      ? null
      : {
          liked,
          likeCount: Math.max(0, summary.likeCount + (liked ? 1 : -1)),
        },
  );

  if (liked) {
    markListStale(queryClient, LIKED_POSTS_PATH);
  } else {
    removeFromCachedList(queryClient, postId, LIKED_POSTS_PATH);
  }
}
