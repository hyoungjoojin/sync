import { describe, expect, it } from 'vitest';

import { PostRecommendationType } from '@/components/feature/post/types/post';

import { createHomeFeedParams } from './homeFeed';

describe('홈 피드 요청 파라미터', () => {
  it.each([
    ['인기', PostRecommendationType.TRENDING],
    ['팔로잉', PostRecommendationType.FOLLOWING],
  ] as const)('%s 탭은 서버에 추천 종류를 전달한다', (_, type) => {
    expect(createHomeFeedParams(type, '50')).toEqual({
      type,
      first: '50',
      after: '',
    });
  });
});
