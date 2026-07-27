'use client';

import { CheckIcon, PlusIcon, StackSimpleIcon } from '@phosphor-icons/react';
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
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';
import { useSession } from '@/lib/auth/client';

import { CollectionFormDialog } from './CollectionFormDialog';
import { useAddPostToCollection } from './hooks/useAddPostToCollection';

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

  const { mutate: addPost, isPending: isAdding } = useAddPostToCollection();

  const add = (externalId: string) => {
    addPost(
      {
        externalId,
        data: { postHandle, projectHandle: projectHandle ?? null },
      },
      {
        onSuccess: () => {
          toast.success(t('messages.success'));
          onOpenChange(false);
        },
        onError: () => toast.error(t('messages.error')),
      },
    );
  };

  const renderList = (
    collections: GetCollectionsResponseCollectionsItem[],
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
          const added = collection.containsPost === true;
          return (
            <Button
              key={collection.externalId}
              variant="ghost"
              className="w-full justify-start"
              disabled={isAdding || added}
              onClick={() => add(collection.externalId)}
            >
              <StackSimpleIcon />
              <span className="truncate">{collection.name}</span>
              {added && <CheckIcon className="text-primary ml-auto shrink-0" />}
            </Button>
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
            <DialogTitle>{t('title')}</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <p className="text-muted-foreground text-xs font-medium">
                {t('sections.personal')}
              </p>
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={() => setCreateOpen(true)}
              >
                <PlusIcon />
                {t('create-new')}
              </Button>
              {renderList(userCollections, isUserPending)}
            </div>

            {projectHandle && isProjectAdmin && (
              <div className="space-y-2">
                <p className="text-muted-foreground text-xs font-medium">
                  {t('sections.project')}
                </p>
                {renderList(projectCollections, isProjectPending)}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <CollectionFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onCreated={(externalId) => add(externalId)}
      />
    </>
  );
}
