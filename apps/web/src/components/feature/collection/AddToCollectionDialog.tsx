'use client';

import { PlusIcon, StackSimpleIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { toast } from 'sonner';

import {
  useGetProjectCollections,
  useGetUserCollections,
} from '@/api/__generated__/collection/collection';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import { GetProjectResponseRole } from '@/api/__generated__/types';
import type { GetCollectionsResponseCollectionsItem } from '@/api/__generated__/types/GetCollectionsResponseCollectionsItem';
import type { GetCollectionsResponseMembershipsItem } from '@/api/__generated__/types/GetCollectionsResponseMembershipsItem';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { useSession } from '@/lib/auth/client';
import { cn } from '@/lib/utils';

import { CollectionFormDialog } from './CollectionFormDialog';
import { useAddPostToCollection } from './hooks/useAddPostToCollection';
import { useRemoveCollectionItem } from './hooks/useRemoveCollectionItem';

interface AddToCollectionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  postHandle: string;
  projectHandle?: string | null;
}

export function AddToCollectionDialog({
  open,
  onOpenChange,
  postHandle,
  projectHandle,
}: AddToCollectionDialogProps) {
  const t = useTranslations('pages.collections.add-to-collection');
  const { data: session } = useSession();
  const userId = session?.user.id;

  const [createOpen, setCreateOpen] = useState(false);
  const [overrides, setOverrides] = useState<Record<string, boolean>>({});
  const [pendingIds, setPendingIds] = useState<Set<string>>(new Set());

  const { data: userData, isPending: isUserPending } = useGetUserCollections(
    String(userId ?? ''),
    { postHandle },
    { query: { enabled: open && !!userId } },
  );

  // 프로젝트 게시글이면, 그 프로젝트의 관리자에게만 프로젝트 컬렉션 섹션을 보인다.
  const { data: project } = useGetProjectByHandle(projectHandle ?? '', {
    query: { enabled: open && !!projectHandle },
  });
  const isProjectAdmin = project?.data.role === GetProjectResponseRole.Admin;

  const { data: projectData, isPending: isProjectPending } =
    useGetProjectCollections(
      projectHandle ?? '',
      { postHandle },
      { query: { enabled: open && !!projectHandle && isProjectAdmin } },
    );

  const userCollections = userData?.data.collections ?? [];
  const projectCollections = projectData?.data.collections ?? [];
  const userMemberships = userData?.data.memberships ?? [];
  const projectMemberships = projectData?.data.memberships ?? [];

  const { mutate: addPost } = useAddPostToCollection();
  const { mutate: removeItem } = useRemoveCollectionItem();

  const setPending = (externalId: string, pending: boolean) => {
    setPendingIds((prev) => {
      const next = new Set(prev);
      if (pending) {
        next.add(externalId);
      } else {
        next.delete(externalId);
      }
      return next;
    });
  };

  const addToNewCollection = (externalId: string) => {
    setOverrides((prev) => ({ ...prev, [externalId]: true }));
    setPending(externalId, true);
    addPost(
      {
        externalId,
        data: { postHandle, projectHandle: projectHandle ?? null },
      },
      {
        onError: () => {
          toast.error(t('messages.error'));
          setOverrides((prev) => ({ ...prev, [externalId]: false }));
        },
        onSettled: () => setPending(externalId, false),
      },
    );
  };

  const toggle = (
    externalId: string,
    collectionPostId: number | null | undefined,
    added: boolean,
  ) => {
    setOverrides((prev) => ({ ...prev, [externalId]: !added }));
    setPending(externalId, true);

    if (added && collectionPostId != null) {
      removeItem(
        { externalId, collectionPostId: String(collectionPostId) },
        {
          onError: () => {
            toast.error(t('messages.remove-error'));
            setOverrides((prev) => ({ ...prev, [externalId]: added }));
          },
          onSettled: () => setPending(externalId, false),
        },
      );
      return;
    }

    addPost(
      {
        externalId,
        data: { postHandle, projectHandle: projectHandle ?? null },
      },
      {
        onError: () => {
          toast.error(t('messages.error'));
          setOverrides((prev) => ({ ...prev, [externalId]: added }));
        },
        onSettled: () => setPending(externalId, false),
      },
    );
  };

  const renderList = (
    collections: GetCollectionsResponseCollectionsItem[],
    memberships: GetCollectionsResponseMembershipsItem[],
    isPending: boolean,
  ) => {
    if (isPending) {
      return (
        <div className="space-y-2">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-10 w-full" />
          ))}
        </div>
      );
    }

    if (collections.length === 0) {
      return (
        <p className="text-muted-foreground py-4 text-center text-sm">
          {t('empty')}
        </p>
      );
    }

    return (
      <div className="max-h-60 space-y-1 overflow-y-auto">
        {collections.map((collection) => {
          const collectionPostId = memberships.find(
            (membership) =>
              membership.collectionExternalId === collection.externalId,
          )?.collectionPostId;
          const added =
            overrides[collection.externalId] ?? collectionPostId != null;
          const isRowPending = pendingIds.has(collection.externalId);
          const inputId = `add-to-collection-${collection.externalId}`;

          return (
            <Label
              key={collection.externalId}
              htmlFor={inputId}
              className={cn(
                'hover:bg-muted w-full cursor-pointer rounded-md px-3 py-2 font-normal',
                isRowPending && 'pointer-events-none opacity-50',
              )}
            >
              <StackSimpleIcon className="shrink-0" />
              <span className="flex-1 truncate">{collection.name}</span>
              <Checkbox
                id={inputId}
                checked={added}
                disabled={isRowPending}
                onCheckedChange={() =>
                  toggle(collection.externalId, collectionPostId, added)
                }
              />
            </Label>
          );
        })}
      </div>
    );
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent>
          <DialogHeader>
            <div className="flex items-center justify-between gap-4">
              <DialogTitle>{t('title')}</DialogTitle>
              <DialogClose />
            </div>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-end">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCreateOpen(true)}
                >
                  <PlusIcon />
                  {t('create-new')}
                </Button>
              </div>
              {renderList(userCollections, userMemberships, isUserPending)}
            </div>

            {projectHandle && isProjectAdmin && (
              <div className="space-y-2">
                <p className="text-muted-foreground text-xs font-medium">
                  {t('sections.project')}
                </p>
                {renderList(
                  projectCollections,
                  projectMemberships,
                  isProjectPending,
                )}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <CollectionFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onCreated={(externalId) => addToNewCollection(externalId)}
      />
    </>
  );
}
