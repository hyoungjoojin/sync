'use client';

import { PencilIcon, PlusIcon, TrashIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslations } from 'next-intl';
import { FormEvent, useState } from 'react';
import { toast } from 'sonner';

import {
  getGetAdminTagsQueryKey,
  useAdminCreateTag,
  useAdminDeleteTag,
  useAdminUnverifyTag,
  useAdminVerifyTag,
  useGetAdminTags,
} from '@/api/__generated__/tag/tag';
import { GetTagsResponseTagsItem } from '@/api/__generated__/types';
import TagEditDialog, {
  TagEditScope,
} from '@/components/feature/tag/TagEditDialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import SyncError, { ErrorCode } from '@/lib/error';

const GLOBAL_TAG_SCOPE: TagEditScope = { type: 'global' };

export default function AdminTagsPage() {
  const t = useTranslations('pages.admin.tags');
  const queryClient = useQueryClient();
  const [newTagName, setNewTagName] = useState('');
  const [editingTag, setEditingTag] = useState<GetTagsResponseTagsItem | null>(
    null,
  );

  const { data, isPending } = useGetAdminTags();
  const tags = data?.data.tags ?? [];

  const invalidateTags = async () => {
    await queryClient.invalidateQueries({
      queryKey: getGetAdminTagsQueryKey(),
    });
  };

  const { mutate: createTag, isPending: isCreating } = useAdminCreateTag({
    mutation: {
      onSuccess: async () => {
        setNewTagName('');
        await invalidateTags();
      },
      onError: (error) => {
        if (
          error instanceof SyncError &&
          error.code === ErrorCode.TAG_ALREADY_EXISTS
        ) {
          toast.error(t('messages.already-exists-error'));
          return;
        }
        toast.error(t('messages.create-error'));
      },
    },
  });

  const { mutate: verifyTag } = useAdminVerifyTag({
    mutation: {
      onSuccess: invalidateTags,
      onError: () => toast.error(t('messages.update-error')),
    },
  });

  const { mutate: unverifyTag } = useAdminUnverifyTag({
    mutation: {
      onSuccess: invalidateTags,
      onError: () => toast.error(t('messages.update-error')),
    },
  });

  const { mutate: deleteTag } = useAdminDeleteTag({
    mutation: {
      onSuccess: invalidateTags,
      onError: () => toast.error(t('messages.delete-error')),
    },
  });

  const handleCreateTag = (event: FormEvent) => {
    event.preventDefault();

    const name = newTagName.trim();
    if (!name) {
      return;
    }

    createTag({ data: { name } });
  };

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-5">
      <div>
        <h1 className="text-2xl font-semibold">{t('title')}</h1>
        <p className="text-muted-foreground">{t('description')}</p>
      </div>

      <form onSubmit={handleCreateTag} className="flex gap-2">
        <Input
          value={newTagName}
          onChange={(event) => setNewTagName(event.target.value)}
          placeholder={t('add-tag.placeholder')}
          className="max-w-xs"
        />
        <Button
          type="submit"
          isPending={isCreating}
          disabled={!newTagName.trim()}
        >
          <PlusIcon />
          {t('add-tag.submit')}
        </Button>
      </form>

      {isPending ? (
        <Skeleton className="h-72 w-full" />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.name')}</TableHead>
              <TableHead>{t('table.description')}</TableHead>
              <TableHead>{t('table.post-count')}</TableHead>
              <TableHead>{t('table.follower-count')}</TableHead>
              <TableHead>{t('table.verified')}</TableHead>
              <TableHead className="text-right">{t('table.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tags.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  {t('empty')}
                </TableCell>
              </TableRow>
            ) : (
              tags.map((tag) => (
                <TableRow key={tag.id}>
                  <TableCell className="font-medium">{tag.name}</TableCell>
                  <TableCell className="max-w-xs truncate text-muted-foreground">
                    {tag.description || '-'}
                  </TableCell>
                  <TableCell>{tag.postCount}</TableCell>
                  <TableCell>{tag.followerCount}</TableCell>
                  <TableCell>
                    {tag.verified ? (
                      <Badge color="success">{t('status.verified')}</Badge>
                    ) : (
                      <Badge variant="outline">{t('status.unverified')}</Badge>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setEditingTag(tag)}
                      >
                        <PencilIcon />
                        {t('actions.edit')}
                      </Button>

                      {tag.verified ? (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => unverifyTag({ name: tag.name })}
                        >
                          {t('actions.unverify')}
                        </Button>
                      ) : (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => verifyTag({ name: tag.name })}
                        >
                          {t('actions.verify')}
                        </Button>
                      )}

                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            size="sm"
                            variant="outline"
                            className="border-destructive/50 text-destructive hover:bg-destructive/10"
                          >
                            <TrashIcon />
                            {t('actions.delete')}
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>
                              {t('delete-dialog.title')}
                            </AlertDialogTitle>
                            <AlertDialogDescription>
                              {t('delete-dialog.description', {
                                name: tag.name,
                              })}
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>
                              {t('delete-dialog.cancel')}
                            </AlertDialogCancel>
                            <AlertDialogAction
                              variant="destructive"
                              onClick={(event) => {
                                event.preventDefault();
                                deleteTag({ name: tag.name });
                              }}
                            >
                              {t('actions.delete')}
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      )}

      <TagEditDialog
        tag={editingTag}
        scope={GLOBAL_TAG_SCOPE}
        onOpenChange={(open) => {
          if (!open) {
            setEditingTag(null);
          }
        }}
        onSuccess={invalidateTags}
      />
    </div>
  );
}
