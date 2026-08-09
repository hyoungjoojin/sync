import {
  useGetMyPostSeries,
  useGetProjectPostSeries,
} from '@/api/__generated__/post-series/post-series';
import type { GetPostSeriesListResponseSeriesItem } from '@/api/__generated__/types';

interface UsePostSeriesListOptions {
  /** 지정되면 프로젝트 시리즈, 없으면 로그인 사용자의 개인 시리즈를 조회한다. */
  projectHandle?: string;
}

/**
 * 시리즈 선택기(에디터)에서 쓰는 목록 조회 훅. 두 컨텍스트 모두 로그인 사용자 본인이
 * 만든 시리즈만 돌려준다. 개인/프로젝트가 서로 다른 엔드포인트(`/me/series` vs
 * `/projects/{handle}/series`)로 분리돼 있으므로, projectHandle 유무로 둘 중 하나만 활성화한다.
 */
export function usePostSeriesList({
  projectHandle,
}: UsePostSeriesListOptions = {}): {
  series: GetPostSeriesListResponseSeriesItem[];
  isPending: boolean;
} {
  const userQuery = useGetMyPostSeries({
    query: { enabled: !projectHandle },
  });
  const projectQuery = useGetProjectPostSeries(projectHandle ?? '', {
    query: { enabled: !!projectHandle },
  });

  if (projectHandle) {
    return {
      series: projectQuery.data?.data.series ?? [],
      isPending: projectQuery.isPending,
    };
  }

  return {
    series: userQuery.data?.data.series ?? [],
    isPending: userQuery.isPending,
  };
}
