import type { GetPostRecommendationsParams } from '@/api/__generated__/types/GetPostRecommendationsParams';
import { PostRecommendationType } from '@/components/feature/post/types/post';

export function createHomeFeedParams(
  type: PostRecommendationType,
  pageSize: string,
): GetPostRecommendationsParams {
  return {
    type,
    first: pageSize,
    after: '',
  };
}
