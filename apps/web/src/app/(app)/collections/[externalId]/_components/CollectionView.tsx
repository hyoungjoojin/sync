'use client';

import {
  DotsThreeIcon,
  GlobeIcon,
  LockIcon,
  PencilSimpleIcon,
  TrashIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';

import { useGetCollection } from '@/api/__generated__/collection/collection';
import { useGetCollectionPostsInfinite } from '@/api/__generated__/collection/collection';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import { GetProjectResponseRole } from '@/api/__generated__/types';
import { CollectionFormDialog } from '@/components/feature/collection/CollectionFormDialog';
import { CollectionItemList } from '@/components/feature/collection/CollectionItemList';
import { useDeleteCollection } from '@/components/feature/collection/hooks/useDeleteCollection';
import {
  COLLECTION_POSTS_PAGE_SIZE,
  toCollectionItem,
} from '@/components/feature/collection/utils';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Skeleton } from '@/components/ui/skeleton';
import { useSession } from '@/lib/auth/client';
import SyncError, { ErrorCode } from '@/lib/error';
import ROUTES from '@/util/routes';

export default function CollectionView({ externalId }: { externalId: string }) {
  const t = useTranslations('pages.collections');
  const router = useRouter();
  const { data: session } = useSession();

  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const { data: collectionData, isPending: isCollectionPending } =
    useGetCollection(externalId);

  const {
    data: postsData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isPending: isPostsPending,
  } = useGetCollectionPostsInfinite(
    externalId,
    { first: COLLECTION_POSTS_PAGE_SIZE, after: '' },
    {
      query: {
        getNextPageParam: (lastPage) => {
          const posts = lastPage.data.posts;
          return posts.pageInfo.hasNextPage
            ? posts.pageInfo.endCursor
            : undefined;
        },
      },
    },
  );

  const { mutate: deleteCollection, isPending: isDeleting } =
    useDeleteCollection();

  const collection = collectionData?.data;

  // 워크스페이스(프로젝트) 컬렉션은 프로젝트 관리자만, 개인 컬렉션은 생성자만
  // 관리할 수 있다. 프로젝트 역할 조회는 워크스페이스일 때만 활성화한다.
  const isWorkspace = collection?.scope === 'WORKSPACE';
  const projectHandle = collection?.projectHandle ?? '';
  const { data: project } = useGetProjectByHandle(projectHandle, {
    query: { enabled: isWorkspace && !!projectHandle },
  });

  if (isCollectionPending) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-1/2" />
        <Skeleton className="h-4 w-2/3" />
      </div>
    );
  }

  if (!collection) {
    return null;
  }

  const canManage = isWorkspace
    ? project?.data.role === GetProjectResponseRole.Admin
    : String(session?.user.id) === String(collection.creatorId);

  const items =
    postsData?.pages.flatMap((page) =>
      page.data.posts.nodes.map((node) => toCollectionItem(node.content)),
    ) ?? [];

  const confirmDelete = () => {
    deleteCollection(
      { externalId },
      {
        onSuccess: () => {
          toast.success(t('messages.delete-success'));
          if (isWorkspace && projectHandle) {
            router.push(ROUTES.PROJECT_COLLECTIONS(projectHandle));
          } else if (session?.user.handle) {
            router.push(ROUTES.PROFILE_COLLECTIONS(session.user.handle));
          } else {
            router.push(ROUTES.HOME());
          }
        },
        onError: (error) => {
          if (
            error instanceof SyncError &&
            error.code === ErrorCode.COLLECTION_NOT_FOUND
          ) {
            toast.error(t('messages.delete-error-not-found'));
            return;
          }
          toast.error(t('messages.delete-error'));
        },
      },
    );
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <div className="flex items-start justify-between gap-2">
          <h1 className="text-2xl font-semibold">{collection.name}</h1>

          {canManage && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  aria-label={t('actions.options')}
                >
                  <DotsThreeIcon weight="bold" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onSelect={() => setEditOpen(true)}>
                  <PencilSimpleIcon />
                  {t('actions.edit')}
                </DropdownMenuItem>
                <DropdownMenuItem
                  variant="destructive"
                  onSelect={() => setDeleteOpen(true)}
                >
                  <TrashIcon />
                  {t('actions.delete')}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>

        {collection.description && (
          <p className="text-muted-foreground text-sm">
            {collection.description}
          </p>
        )}

        <div className="text-muted-foreground flex items-center gap-2 text-sm">
          <Badge variant="secondary" className="gap-1">
            {collection.isPublic ? (
              <GlobeIcon className="size-3" />
            ) : (
              <LockIcon className="size-3" />
            )}
            {collection.isPublic
              ? t('visibility.public')
              : t('visibility.private')}
          </Badge>
          <span>{t('post-count', { count: collection.postCount })}</span>
        </div>
      </div>

      <CollectionItemList
        items={items}
        isPending={isPostsPending}
        hasNextPage={!!hasNextPage}
        isFetchingNextPage={isFetchingNextPage}
        fetchNextPage={fetchNextPage}
      />

      <CollectionFormDialog
        open={editOpen}
        onOpenChange={setEditOpen}
        collection={{
          externalId,
          name: collection.name,
          description: collection.description,
          isPublic: collection.isPublic,
        }}
      />

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t('delete-confirm.title')}</AlertDialogTitle>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t('actions.cancel')}</AlertDialogCancel>
            <AlertDialogAction
              variant="destructive"
              disabled={isDeleting}
              onClick={confirmDelete}
            >
              {t('actions.delete')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
