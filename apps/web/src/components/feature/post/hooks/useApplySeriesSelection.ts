import { useQueryClient } from '@tanstack/react-query';
import { useCallback } from 'react';

import {
  addPostToPostSeries,
  createPostSeries,
  createProjectPostSeries,
  getGetMyPostSeriesQueryKey,
  getGetProjectPostSeriesQueryKey,
  getGetSeriesForPostQueryKey,
  removePostFromPostSeries,
} from '@/api/__generated__/post-series/post-series';
import { getGetPostBySlugQueryKey } from '@/api/__generated__/post/post';

import type { SeriesSelection } from '../editor/components/SeriesSelect';

/** 편집 진입 시점의 시리즈 소속. 없으면 어떤 시리즈에도 속하지 않은 상태. */
export interface InitialSeries {
  seriesId: string;
  seriesPostId: number;
}

interface ApplySeriesParams {
  /** 게시글 slug(핸들). 생성 게시글이면 저장 후 확정된 slug 를 넘긴다. */
  slug: string;
  /** 워크스페이스 게시글이면 소속 프로젝트 핸들, 개인 게시글이면 undefined. */
  projectHandle?: string;
  /** 사용자가 선택한 시리즈. 새 시리즈 생성이거나 기존 시리즈이거나 null(선택 없음). */
  selection: SeriesSelection | null;
  /** 저장 전의 소속 상태. 편집 페이지에서만 존재한다. */
  initial: InitialSeries | null;
}

/**
 * 저장 시점에 게시글의 시리즈 소속을 서버와 맞춘다. 게시글 저장(생성/수정)과 시리즈
 * 편성은 별개 엔드포인트라 2-step 으로 처리한다: 소속이 바뀌었으면 기존 편성을 제거한
 * 뒤, 새 시리즈(필요하면 생성)에 게시글을 추가한다. 추가는 항상 맨 뒤에 붙이므로
 * position 충돌이 없다(순서 조정은 상세 카드에서 별도로 한다). 끝나면 게시글의 시리즈
 * 조회 캐시와 시리즈 목록 캐시를, 그리고 `isSeriesPost` 가 바뀌는 상세 캐시를 무효화한다.
 */
export function useApplySeriesSelection() {
  const queryClient = useQueryClient();

  return useCallback(
    async ({ slug, projectHandle, selection, initial }: ApplySeriesParams) => {
      const unchanged =
        selection?.kind === 'existing' &&
        initial != null &&
        selection.externalId === initial.seriesId;

      if (unchanged || (selection == null && initial == null)) {
        return;
      }

      // 소속이 바뀌거나 해제됐으면 기존 편성부터 제거한다.
      if (initial != null) {
        await removePostFromPostSeries(
          initial.seriesId,
          String(initial.seriesPostId),
        );
      }

      if (selection != null) {
        let externalId: string;
        if (selection.kind === 'new') {
          const created = projectHandle
            ? await createProjectPostSeries(projectHandle, {
                name: selection.name,
              })
            : await createPostSeries({ name: selection.name });
          externalId = created.data.externalId;
        } else {
          externalId = selection.externalId;
        }

        await addPostToPostSeries(externalId, {
          postHandle: slug,
          projectHandle: projectHandle ?? null,
        });
      }

      await queryClient.invalidateQueries({
        queryKey: getGetSeriesForPostQueryKey(slug),
      });
      queryClient.invalidateQueries({
        queryKey: getGetPostBySlugQueryKey(slug),
      });
      if (projectHandle) {
        queryClient.invalidateQueries({
          queryKey: getGetProjectPostSeriesQueryKey(projectHandle),
        });
      } else {
        queryClient.invalidateQueries({
          queryKey: getGetMyPostSeriesQueryKey(),
        });
      }
    },
    [queryClient],
  );
}
