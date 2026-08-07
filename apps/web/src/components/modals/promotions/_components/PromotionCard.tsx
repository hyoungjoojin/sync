'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useMemo } from 'react';
import { Controller, Resolver, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import type { ActivePromotionResponseListItem } from '@/api/__generated__/types/ActivePromotionResponseListItem';
import { ActivePromotionResponseListItemFieldsItemType } from '@/api/__generated__/types/ActivePromotionResponseListItemFieldsItemType';
import { useSubmitPromotionSignup } from '@/components/feature/promotion/hooks/useSubmitPromotionSignup';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';

import PhoneNumberInput from './PhoneNumberInput';
import PromotionPostPreview from './PromotionPostPreview';

const PHONE_NUMBER_PATTERN = /^01[0-9]{8,9}$/;

type FormValues = Record<string, string>;

interface PromotionCardProps {
  promotion: ActivePromotionResponseListItem;
}

export default function PromotionCard({ promotion }: PromotionCardProps) {
  const t = useTranslations('modals.promotions');

  const fields = promotion.fields;
  const hasSignedUp = Boolean(promotion.signup);

  const schema = useMemo(() => {
    const shape: Record<string, z.ZodType<string>> = {};

    for (const field of fields) {
      if (
        field.type === ActivePromotionResponseListItemFieldsItemType.Checkbox
      ) {
        shape[field.key] = field.required
          ? z.literal('true', { error: t('form.errors.required') })
          : z.string();
        continue;
      }

      let fieldSchema = z.string();
      if (field.required) {
        fieldSchema = fieldSchema.min(1, { error: t('form.errors.required') });
      }
      if (
        field.type === ActivePromotionResponseListItemFieldsItemType.PhoneNumber
      ) {
        fieldSchema = fieldSchema.refine(
          (value) => value.length === 0 || PHONE_NUMBER_PATTERN.test(value),
          { error: t('form.errors.invalid') },
        );
      }
      shape[field.key] = fieldSchema;
    }

    return z.object(shape);
  }, [fields, t]);

  const defaultValues = useMemo(() => {
    const attachment = promotion.signup?.attachment as
      | Record<string, string | null | undefined>
      | undefined;
    const values: FormValues = {};
    for (const field of fields) {
      values[field.key] = attachment?.[field.key] ?? '';
    }
    return values;
  }, [fields, promotion.signup]);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema) as unknown as Resolver<FormValues>,
    defaultValues,
  });

  const { mutate: submitSignup, isPending } = useSubmitPromotionSignup(
    promotion.id,
    {
      onSuccess: () => {
        toast.success(t('messages.success'));
      },
      onError: () => {
        toast.error(t('messages.error'));
      },
    },
  );

  const onFormSubmit = (values: FormValues) => {
    submitSignup(values);
  };

  // Every promotion links to a post — creation always goes through the flow that
  // authors one first. A missing link means stale/legacy data, not a UI state to design for.
  if (!promotion.projectHandle || !promotion.postSlug) {
    return null;
  }

  return (
    <div className="flex flex-col gap-4">
      <PromotionPostPreview
        projectHandle={promotion.projectHandle}
        postSlug={promotion.postSlug}
      />

      {fields.length > 0 && (
        <>
          <Separator />
          <form onSubmit={form.handleSubmit(onFormSubmit)}>
            <FieldGroup>
              {fields.map((field) => (
                <Controller
                  key={field.key}
                  name={field.key}
                  control={form.control}
                  render={({ field: formField, fieldState }) => {
                    const fieldId = `promotion-${promotion.id}-${field.key}`;

                    if (
                      field.type ===
                      ActivePromotionResponseListItemFieldsItemType.Checkbox
                    ) {
                      return (
                        <Field
                          data-invalid={fieldState.invalid}
                          orientation="horizontal"
                        >
                          <Checkbox
                            id={fieldId}
                            checked={formField.value === 'true'}
                            onCheckedChange={(checked) =>
                              formField.onChange(String(checked === true))
                            }
                            aria-invalid={fieldState.invalid}
                          />
                          <FieldLabel htmlFor={fieldId}>
                            {field.label}
                          </FieldLabel>
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      );
                    }

                    if (
                      field.type ===
                      ActivePromotionResponseListItemFieldsItemType.PhoneNumber
                    ) {
                      return (
                        <Field
                          data-invalid={fieldState.invalid}
                          orientation="horizontal"
                        >
                          <FieldLabel htmlFor={fieldId}>
                            {field.label}
                          </FieldLabel>
                          <PhoneNumberInput
                            id={fieldId}
                            value={formField.value}
                            onChange={formField.onChange}
                            onBlur={formField.onBlur}
                            aria-invalid={fieldState.invalid}
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      );
                    }

                    return (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor={fieldId}>{field.label}</FieldLabel>
                        <Textarea
                          {...formField}
                          id={fieldId}
                          rows={3}
                          aria-invalid={fieldState.invalid}
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    );
                  }}
                />
              ))}

              <div className="flex justify-end">
                <Button type="submit" isPending={isPending}>
                  {hasSignedUp
                    ? t('form.submit.update')
                    : t('form.submit.apply')}
                </Button>
              </div>
            </FieldGroup>
          </form>
        </>
      )}
    </div>
  );
}
