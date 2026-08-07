'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { Control, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import { useCreateProjectPost } from '@/api/__generated__/post/post';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import {
  getGetPromotionsQueryKey,
  useCreatePromotion,
} from '@/api/__generated__/promotion/promotion';
import { CreatePromotionRequestFieldsItemType } from '@/api/__generated__/types/CreatePromotionRequestFieldsItemType';
import PostEditor from '@/components/feature/post/editor/PostEditor';
import { PostType } from '@/components/feature/post/types/post';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import ROUTES from '@/util/routes';

import PromotionFieldsBuilder, {
  type PromotionFieldFormValue,
  type PromotionFieldsBuilderFormValues,
} from '../_components/PromotionFieldsBuilder';

interface PromotionFormValues {
  fields: PromotionFieldFormValue[];
}

export default function NewPromotionPage() {
  const t = useTranslations('pages.admin.promotions');
  const router = useRouter();
  const queryClient = useQueryClient();

  const [projectHandle, setProjectHandle] = useState('');
  const { data: projectData } = useGetProjectByHandle(projectHandle, {
    query: { enabled: projectHandle.length > 0 },
  });

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

  const promotionForm = useForm<PromotionFormValues>({
    resolver: zodResolver(schema),
    defaultValues: { fields: [] },
  });

  const { mutate: createProjectPost, isPending: isCreatingPost } =
    useCreateProjectPost();
  const { mutate: createPromotion, isPending: isCreatingPromotion } =
    useCreatePromotion();

  const handlePostSubmit: React.ComponentProps<
    typeof PostEditor
  >['onSubmit'] = async ({
    title,
    type,
    status,
    tags,
    projectTags,
    coverMediaId,
    content,
  }) => {
    if (!projectHandle) {
      toast.error(t('new.errors.project-handle-required'));
      return;
    }

    const valid = await promotionForm.trigger();
    if (!valid) {
      toast.error(t('new.errors.promotion-invalid'));
      return;
    }

    createProjectPost(
      {
        handle: projectHandle,
        data: {
          type,
          status,
          title,
          tags,
          projectTags,
          coverMediaId,
          content: {
            json: content.json,
            text: content.text,
            mediaIds: content.media.map((media) => media.id),
          },
        },
      },
      {
        onSuccess: ({ data: post }) => {
          const promotionValues = promotionForm.getValues();
          createPromotion(
            {
              data: {
                fields: promotionValues.fields,
                projectHandle,
                postSlug: post.slug,
              },
            },
            {
              onSuccess: async () => {
                toast.success(t('new.messages.success'));
                await queryClient.invalidateQueries({
                  queryKey: getGetPromotionsQueryKey(),
                });
                router.push(ROUTES.ADMIN_PROMOTIONS());
              },
              onError: () => {
                toast.error(
                  t('new.errors.promotion-create-failed', { slug: post.slug }),
                );
              },
            },
          );
        },
        onError: () => {
          toast.error(t('new.errors.post-create-failed'));
        },
      },
    );
  };

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
      <h1 className="text-2xl font-semibold">{t('new.title')}</h1>

      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="new-promotion-project-handle">
            {t('new.fields.project-handle.label')}
          </FieldLabel>
          <Input
            id="new-promotion-project-handle"
            value={projectHandle}
            onChange={(event) => setProjectHandle(event.target.value)}
            placeholder={t('new.fields.project-handle.placeholder')}
          />
          {projectHandle && (
            <p className="text-muted-foreground text-xs">
              {projectData?.data.summary.name
                ? t('new.fields.project-handle.found', {
                    name: projectData.data.summary.name,
                  })
                : t('new.fields.project-handle.not-found')}
            </p>
          )}
        </Field>

        <PromotionFieldsBuilder
          control={
            promotionForm.control as unknown as Control<PromotionFieldsBuilderFormValues>
          }
        />
      </FieldGroup>

      <PostEditor
        type={PostType.LONG}
        isSubmitting={isCreatingPost || isCreatingPromotion}
        project={{
          handle: projectHandle,
          name: projectData?.data.summary.name ?? projectHandle,
        }}
        onSubmit={handlePostSubmit}
      />
    </div>
  );
}
