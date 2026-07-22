import { useGetSeriesForPost } from '@/api/__generated__/post-series/post-series';

interface PostSeriesMembership {
  /** 에디터의 시리즈 선택기 초기값 (시리즈에 속하지 않으면 null) */
  initialSeries: { seriesId: string; seriesName: string } | null;
  /** 저장 시점에 기존 소속과 비교하기 위한 값 (시리즈에 속하지 않으면 null) */
  initialSeriesMembership: { seriesId: string; seriesPostId: number } | null;
  isPending: boolean;
}

/**
 * 게시글이 속한 시리즈를 `GET /posts/{slug}/series` 에서 직접 조회해 에디터 초기값으로 바꾼다.
 * 게시글 상세 응답에는 시리즈 정보가 실리지 않으므로(상세 읽기 경로를 가볍게 유지) 편집 화면은
 * 이 훅으로 따로 가져온다. `PostEditor` 는 초기값을 마운트 시점에 한 번만 읽으므로, 호출부는
 * `isPending` 인 동안 에디터를 렌더링하지 않아야 프리필이 유실되지 않는다.
 */
export function usePostSeriesMembership(slug: string): PostSeriesMembership {
  const { data, isPending } = useGetSeriesForPost(slug);

  const series = data?.data.series;
  const seriesId = series?.externalId;
  const seriesName = series?.name;
  const seriesPostId = data?.data.currentSeriesPostId;

  return {
    initialSeries: seriesId && seriesName ? { seriesId, seriesName } : null,
    initialSeriesMembership:
      seriesId && seriesPostId != null ? { seriesId, seriesPostId } : null,
    isPending,
  };
}
