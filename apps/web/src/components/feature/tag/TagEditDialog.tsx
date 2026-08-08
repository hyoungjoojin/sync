'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import { useUpdateProjectTag, useUpdateTag } from '@/api/__generated__/tag/tag';
import { GetTagsResponseTagsItem } from '@/api/__generated__/types';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import SyncError, { ErrorCode } from '@/lib/error';

export type TagEditScope =
  | { type: 'global' }
  | { type: 'project'; handle: string };

interface TagEditDialogProps {
  tag: GetTagsResponseTagsItem | null;
  scope: TagEditScope;
  onOpenChange: (open: boolean) => void;
  onSuccess: (updatedTag: { name: string; description: string | null }) => void;
}

const TagEditFormSchema = z.object({
  name: z.string().trim().min(1),
  description: z.string().trim(),
});

type TagEditFormValues = z.infer<typeof TagEditFormSchema>;

export default function TagEditDialog({
  tag,
  scope,
  onOpenChange,
  onSuccess,
}: TagEditDialogProps) {
  const t = useTranslations('components.tag.edit-dialog');

  const form = useForm<TagEditFormValues>({
    resolver: zodResolver(TagEditFormSchema),
    defaultValues: { name: '', description: '' },
  });

  useEffect(() => {
    if (tag) {
      form.reset({ name: tag.name, description: tag.description ?? '' });
    }
  }, [tag, form]);

  const { mutate: updateTag, isPending: isUpdatingGlobalTag } = useUpdateTag();
  const { mutate: updateProjectTag, isPending: isUpdatingProjectTag } =
    useUpdateProjectTag();
  const isPending = isUpdatingGlobalTag || isUpdatingProjectTag;

  const onSubmit = form.handleSubmit((values) => {
    if (!tag) {
      return;
    }

    const description =
      values.description.length > 0 ? values.description : null;

    const onMutationSuccess = () => {
      toast.success(t('messages.success'));
      onSuccess({ name: values.name, description });
      onOpenChange(false);
    };

    const onMutationError = (error: unknown) => {
      if (
        error instanceof SyncError &&
        error.code === ErrorCode.TAG_ALREADY_EXISTS
      ) {
        form.setError('name', {
          type: 'server',
          message: t('messages.already-exists'),
        });
        return;
      }
      toast.error(t('messages.error'));
    };

    if (scope.type === 'global') {
      updateTag(
        { name: tag.name, data: { name: values.name, description } },
        { onSuccess: onMutationSuccess, onError: onMutationError },
      );
      return;
    }

    updateProjectTag(
      {
        handle: scope.handle,
        name: tag.name,
        data: { name: values.name, description },
      },
      { onSuccess: onMutationSuccess, onError: onMutationError },
    );
  });

  return (
    <Dialog open={tag !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('title')}</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <FieldGroup>
            <Field data-invalid={!!form.formState.errors.name}>
              <div className="flex items-center justify-between">
                <FieldLabel htmlFor="tag-edit-name">
                  {t('fields.name.label')}
                </FieldLabel>
                {form.formState.errors.name && (
                  <FieldError errors={[form.formState.errors.name]} />
                )}
              </div>
              <Input
                id="tag-edit-name"
                {...form.register('name')}
                aria-invalid={!!form.formState.errors.name}
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="tag-edit-description">
                {t('fields.description.label')}
              </FieldLabel>
              <Textarea
                id="tag-edit-description"
                {...form.register('description')}
                placeholder={t('fields.description.placeholder')}
              />
            </Field>
          </FieldGroup>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              {t('cancel')}
            </Button>
            <Button
              type="submit"
              isPending={isPending}
              disabled={!form.formState.isDirty || isPending}
            >
              {t('submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
