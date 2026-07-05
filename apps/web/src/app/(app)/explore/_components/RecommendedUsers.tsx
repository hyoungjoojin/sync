'use client';

import { useQueryClient } from '@tanstack/react-query';

import {
  getGetRecommendationsQueryOptions,
  useFollowUser,
  useGetRecommendations,
} from '@/api/__generated__/user/user';
import { Button } from '@/components/ui/button';
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from '@/components/ui/carousel';
import { Skeleton } from '@/components/ui/skeleton';

export default function RecommendedUsers() {
  const queryClient = useQueryClient();

  const { data, isPending } = useGetRecommendations();
  const { mutate: followUser, isPending: isFollowPending } = useFollowUser();

  if (isPending) {
    return <RecommendedUsersSkeleton />;
  }

  const users = data?.data.users ?? [];

  if (users.length === 0) {
    return null;
  }

  const invalidateRecommendations = async () => {
    await queryClient.invalidateQueries(getGetRecommendationsQueryOptions());
  };

  return (
    <div className="flex flex-col gap-4">
      <p className="text-lg font-semibold">추천 사용자</p>

      <Carousel opts={{ align: 'start' }}>
        <CarouselContent>
          {users.map((user) => (
            <CarouselItem
              key={user.userId}
              className="basis-1/2 sm:basis-1/3 md:basis-1/4 lg:basis-1/5"
            >
              <div className="flex flex-col items-center gap-3 rounded-lg border p-4 text-center">
                <div className="bg-primary text-primary-foreground flex size-14 items-center justify-center rounded-2xl text-xl font-semibold">
                  {user.name.charAt(0).toUpperCase()}
                </div>

                <div>
                  <p className="truncate text-sm font-semibold">{user.name}</p>
                  <p className="text-muted-foreground truncate text-xs">
                    @{user.handle}
                  </p>
                </div>

                <Button
                  className="w-full"
                  size="sm"
                  disabled={isFollowPending}
                  onClick={() =>
                    followUser(
                      { followeeId: user.userId },
                      { onSuccess: invalidateRecommendations },
                    )
                  }
                >
                  팔로우
                </Button>
              </div>
            </CarouselItem>
          ))}
        </CarouselContent>

        <CarouselPrevious />
        <CarouselNext />
      </Carousel>
    </div>
  );
}

function RecommendedUsersSkeleton() {
  return (
    <div className="flex flex-col gap-4">
      <Skeleton className="h-6 w-24" />

      <div className="flex gap-4">
        {Array.from({ length: 5 }).map((_, index) => (
          <div
            key={index}
            className="flex basis-1/5 flex-col items-center gap-3 rounded-lg border p-4"
          >
            <Skeleton className="size-14 rounded-2xl" />
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-8 w-full" />
          </div>
        ))}
      </div>
    </div>
  );
}
