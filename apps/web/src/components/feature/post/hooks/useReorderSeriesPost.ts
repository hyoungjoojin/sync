import { useQueryClient } from '@tanstack/react-query';

import {
  getGetSeriesForPostQueryKey,
  useReorderPostSeriesPost as useReorderPostSeriesPostMutation,
} from '@/api/__generated__/post-series/post-series';
import type { GetPostSeriesResponse } from '@/api/__generated__/types';

type SeriesPostsData = { data: GetPostSeriesResponse };

/**
 * 시리즈 내 순서를 인접 항목과 맞바꾼다(위/아래로 한 칸). 서버는 목표 position 을 받아
 * 사이 항목을 밀어 재정렬하지만, 클라이언트는 낙관적으로 두 항목의 position 을 맞바꾸고
 * position 오름차순으로 다시 정렬해 순간적인 충돌 없이 보이도록 한다. 실패하면 이전
 * 스냅샷으로 되돌리고, 성공/실패와 무관하게 시리즈 캐시를 무효화해 서버 상태로 수렴한다.
 */
export function useReorderSeriesPost(slug: string) {
  const queryClient = useQueryClient();
  const postsKey = getGetSeriesForPostQueryKey(slug);

  return useReorderPostSeriesPostMutation({
    mutation: {
      onMutate: async ({ seriesPostId, data }) => {
        await queryClient.cancelQueries({ queryKey: postsKey });
        const previous = queryClient.getQueryData<SeriesPostsData>(postsKey);

        const targetPosition = data?.position;
        queryClient.setQueryData<SeriesPostsData>(postsKey, (old) => {
          if (!old || targetPosition == null) {
            return old;
          }

          const movedId = Number(seriesPostId);
          const moved = old.data.posts.find(
            (item) => item.seriesPostId === movedId,
          );
          const displaced = old.data.posts.find(
            (item) => item.position === targetPosition,
          );
          if (!moved || !displaced) {
            return old;
          }

          const fromPosition = moved.position;
          const posts = old.data.posts
            .map((item) => {
              if (item.seriesPostId === movedId) {
                return { ...item, position: targetPosition };
              }
              if (item.seriesPostId === displaced.seriesPostId) {
                return { ...item, position: fromPosition };
              }
              return item;
            })
            .sort((a, b) => a.position - b.position);

          return { ...old, data: { ...old.data, posts } };
        });

        return { previous };
      },
      onError: (_error, _variables, context) => {
        if (context?.previous) {
          queryClient.setQueryData(postsKey, context.previous);
        }
      },
      onSettled: () => {
        queryClient.invalidateQueries({ queryKey: postsKey });
      },
    },
  });
}
