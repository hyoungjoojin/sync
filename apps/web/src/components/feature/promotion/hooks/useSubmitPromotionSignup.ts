import { useQueryClient } from '@tanstack/react-query';

import {
  getGetActivePromotionsQueryKey,
  useSubmitPromotionSignup as useSubmitPromotionSignupMutation,
} from '@/api/__generated__/promotion/promotion';

interface UseSubmitPromotionSignupOptions {
  onSuccess?: () => void;
  onError?: () => void;
}

export function useSubmitPromotionSignup(
  promotionId: number,
  options?: UseSubmitPromotionSignupOptions,
) {
  const queryClient = useQueryClient();

  const mutation = useSubmitPromotionSignupMutation({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getGetActivePromotionsQueryKey(),
        });

        options?.onSuccess?.();
      },
      onError: options?.onError,
    },
  });

  return {
    ...mutation,
    mutate: (attachment: Record<string, string>) =>
      mutation.mutate({
        promotionId: String(promotionId),
        data: { attachment },
      }),
  };
}
