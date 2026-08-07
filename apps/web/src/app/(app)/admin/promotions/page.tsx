'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { FileTextIcon, PlusIcon, UsersIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { Control, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import {
  getGetPromotionsQueryKey,
  useActivatePromotion,
  useDeactivatePromotion,
  useGetPromotions,
  useUpdatePromotion,
} from '@/api/__generated__/promotion/promotion';
import { CreatePromotionRequestFieldsItemType } from '@/api/__generated__/types/CreatePromotionRequestFieldsItemType';
import type { PromotionResponseListItem } from '@/api/__generated__/types/PromotionResponseListItem';
import { Badge } from '@/components/ui/badge';
import { Button, LinkButton } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { FieldGroup } from '@/components/ui/field';
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
import ROUTES from '@/util/routes';

import PromotionFieldsBuilder, {
  type PromotionFieldFormValue,
  type PromotionFieldsBuilderFormValues,
} from './_components/PromotionFieldsBuilder';
import PromotionSignupsDialog from './_components/PromotionSignupsDialog';

interface PromotionFormValues {
  fields: PromotionFieldFormValue[];
}

const emptyFormValues: PromotionFormValues = {
  fields: [],
};

export default function AdminPromotionsPage() {
  const t = useTranslations('pages.admin.promotions');
  const queryClient = useQueryClient();

  const [editingPromotion, setEditingPromotion] =
    useState<PromotionResponseListItem | null>(null);
  const [viewingSignupsFor, setViewingSignupsFor] =
    useState<PromotionResponseListItem | null>(null);

  const { data, isPending } = useGetPromotions();
  const promotions = data?.data ?? [];

  const invalidatePromotions = async () => {
    await queryClient.invalidateQueries({
      queryKey: getGetPromotionsQueryKey(),
    });
  };

  const { mutate: updatePromotion, isPending: isUpdatePending } =
    useUpdatePromotion();
  const { mutate: activatePromotion } = useActivatePromotion();
  const { mutate: deactivatePromotion } = useDeactivatePromotion();

  const schema = z.object({
    fields: z
      .array(
        z.object({
          key: z
            .string()
            .min(1, { error: t('form.errors.required_field_key') }),
          type: z.enum(CreatePromotionRequestFieldsItemType),
          label: z
            .string()
            .min(1, { error: t('form.errors.required_field_label') }),
          required: z.boolean(),
        }),
      )
      .refine(
        (fields) =>
          new Set(fields.map((field) => field.key)).size === fields.length,
        { error: t('form.errors.duplicate_field_key') },
      ),
  });

  const editForm = useForm<PromotionFormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyFormValues,
  });

  const openEditDialog = (promotion: PromotionResponseListItem) => {
    editForm.reset({
      fields: promotion.fields.map((field) => ({
        key: field.key,
        type: field.type,
        label: field.label,
        required: field.required,
      })),
    });
    setEditingPromotion(promotion);
  };

  const onEditSubmit = (values: PromotionFormValues) => {
    // Every promotion links to a post, but the response type stays nullable for
    // legacy rows predating that requirement — bail rather than send an invalid update.
    if (
      !editingPromotion ||
      !editingPromotion.projectHandle ||
      !editingPromotion.postSlug
    ) {
      return;
    }

    updatePromotion(
      {
        id: String(editingPromotion.id),
        data: {
          ...values,
          // The edit dialog doesn't touch the linked post — carry the existing
          // link forward unchanged instead of wiping it (update is a full replace).
          projectHandle: editingPromotion.projectHandle,
          postSlug: editingPromotion.postSlug,
        },
      },
      {
        onSuccess: async () => {
          toast.success(t('messages.update-success'));
          setEditingPromotion(null);
          await invalidatePromotions();
        },
        onError: async (error) => {
          if (error instanceof SyncError) {
            switch (error.code) {
              case ErrorCode.PROMOTION_NOT_FOUND:
                toast.error(t('messages.not-found-error'));
                setEditingPromotion(null);
                await invalidatePromotions();
                return;
              case ErrorCode.POST_NOT_FOUND:
                toast.error(t('messages.linked-post-not-found-error'));
                return;
            }
          }
          toast.error(t('messages.save-error'));
        },
      },
    );
  };

  const toggleActive = (promotion: PromotionResponseListItem) => {
    const mutate = promotion.active ? deactivatePromotion : activatePromotion;

    mutate(
      { id: String(promotion.id) },
      {
        onSuccess: async () => {
          toast.success(
            promotion.active
              ? t('messages.deactivate-success')
              : t('messages.activate-success'),
          );
          await invalidatePromotions();
        },
        onError: async (error) => {
          if (error instanceof SyncError) {
            switch (error.code) {
              case ErrorCode.PROMOTION_NOT_FOUND:
                toast.error(t('messages.not-found-error'));
                await invalidatePromotions();
                return;
            }
          }
          toast.error(t('messages.toggle-error'));
        },
      },
    );
  };

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{t('title')}</h1>
        <LinkButton href={ROUTES.ADMIN_NEW_PROMOTION()}>
          <PlusIcon />
          {t('actions.create')}
        </LinkButton>
      </div>

      {isPending ? (
        <Skeleton className="h-72 w-full" />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.post-title')}</TableHead>
              <TableHead>{t('table.status')}</TableHead>
              <TableHead>{t('table.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {promotions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3} className="h-24 text-center">
                  {t('empty')}
                </TableCell>
              </TableRow>
            ) : (
              promotions.map((promotion) => (
                <TableRow key={promotion.id}>
                  <TableCell className="font-medium">
                    {promotion.postTitle}
                  </TableCell>
                  <TableCell>
                    <Badge variant={promotion.active ? 'default' : 'outline'}>
                      {promotion.active
                        ? t('statuses.active')
                        : t('statuses.inactive')}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-2">
                      {promotion.projectHandle && promotion.postSlug && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => openEditDialog(promotion)}
                        >
                          {t('actions.edit')}
                        </Button>
                      )}
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setViewingSignupsFor(promotion)}
                      >
                        <UsersIcon />
                        {t('actions.view-signups')}
                      </Button>
                      {promotion.projectHandle && promotion.postSlug && (
                        <LinkButton
                          size="sm"
                          variant="outline"
                          href={ROUTES.PROJECT_POST(
                            promotion.projectHandle,
                            promotion.postSlug,
                          )}
                        >
                          <FileTextIcon />
                          {t('actions.view-post')}
                        </LinkButton>
                      )}
                      <Button
                        size="sm"
                        variant={promotion.active ? 'destructive' : 'default'}
                        onClick={() => toggleActive(promotion)}
                      >
                        {promotion.active
                          ? t('actions.deactivate')
                          : t('actions.activate')}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      )}

      <Dialog
        open={editingPromotion !== null}
        onOpenChange={(open) => {
          if (!open) {
            setEditingPromotion(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t('edit-dialog.title')}</DialogTitle>
          </DialogHeader>

          <form onSubmit={editForm.handleSubmit(onEditSubmit)}>
            <FieldGroup>
              <PromotionFieldsBuilder
                control={
                  editForm.control as unknown as Control<PromotionFieldsBuilderFormValues>
                }
              />
            </FieldGroup>

            <DialogFooter className="mt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setEditingPromotion(null)}
              >
                {t('actions.cancel')}
              </Button>
              <Button type="submit" isPending={isUpdatePending}>
                {t('actions.save')}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <PromotionSignupsDialog
        promotion={viewingSignupsFor}
        onOpenChange={(open) => {
          if (!open) {
            setViewingSignupsFor(null);
          }
        }}
      />
    </div>
  );
}
