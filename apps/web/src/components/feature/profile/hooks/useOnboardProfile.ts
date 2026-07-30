import { useQueryClient } from '@tanstack/react-query';

import {
  getGetAuthenticatedUserQueryKey,
  useOnboardProfile as useOnboardProfileMutation,
} from '@/api/__generated__/profile/profile';
import { getGetUserRecommendationsQueryKey } from '@/api/__generated__/user/user';

interface UseOnboardProfileOptions {
  onSuccess?: () => void;
  onError?: () => void;
}

export function useOnboardProfile(options?: UseOnboardProfileOptions) {
  const queryClient = useQueryClient();

  return useOnboardProfileMutation({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getGetAuthenticatedUserQueryKey(),
        });

        queryClient.removeQueries({
          queryKey: getGetUserRecommendationsQueryKey(),
        });

        options?.onSuccess?.();
      },
      onError: options?.onError,
    },
  });
}
