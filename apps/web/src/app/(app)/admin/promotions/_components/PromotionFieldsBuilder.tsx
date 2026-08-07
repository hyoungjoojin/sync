'use client';

import { PlusIcon, TrashIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { Control, Controller, useFieldArray } from 'react-hook-form';

import { CreatePromotionRequestFieldsItemType } from '@/api/__generated__/types/CreatePromotionRequestFieldsItemType';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Field, FieldError, FieldGroup } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export interface PromotionFieldFormValue {
  key: string;
  type: CreatePromotionRequestFieldsItemType;
  label: string;
  required: boolean;
}

export interface PromotionFieldsBuilderFormValues {
  fields: PromotionFieldFormValue[];
}

const FIELD_TYPES = Object.values(CreatePromotionRequestFieldsItemType);

interface PromotionFieldsBuilderProps {
  control: Control<PromotionFieldsBuilderFormValues>;
}

export default function PromotionFieldsBuilder({
  control,
}: PromotionFieldsBuilderProps) {
  const t = useTranslations('pages.admin.promotions');
  const { fields, append, remove } = useFieldArray({ control, name: 'fields' });

  return (
    <FieldGroup>
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium">
          {t('form.fields.fields.label')}
        </span>
        <Button
          type="button"
          size="sm"
          variant="outline"
          onClick={() =>
            append({
              key: '',
              type: CreatePromotionRequestFieldsItemType.Text,
              label: '',
              required: true,
            })
          }
        >
          <PlusIcon />
          {t('actions.add-field')}
        </Button>
      </div>

      {fields.length === 0 && (
        <p className="text-muted-foreground text-xs">
          {t('form.fields.fields.empty')}
        </p>
      )}

      {fields.map((field, index) => (
        <div
          key={field.id}
          className="grid grid-cols-[1fr_1fr_1fr_auto_auto] items-end gap-2 rounded-md border p-3"
        >
          <Controller
            name={`fields.${index}.key`}
            control={control}
            render={({ field: formField, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <Input
                  {...formField}
                  placeholder={t('form.fields.fields.key-placeholder')}
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid && (
                  <FieldError errors={[fieldState.error]} />
                )}
              </Field>
            )}
          />

          <Controller
            name={`fields.${index}.label`}
            control={control}
            render={({ field: formField, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <Input
                  {...formField}
                  placeholder={t('form.fields.fields.label-placeholder')}
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid && (
                  <FieldError errors={[fieldState.error]} />
                )}
              </Field>
            )}
          />

          <Controller
            name={`fields.${index}.type`}
            control={control}
            render={({ field: formField }) => (
              <Select
                value={formField.value}
                onValueChange={formField.onChange}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {FIELD_TYPES.map((type) => (
                    <SelectItem key={type} value={type}>
                      {t(`field-types.${type}`)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          />

          <Controller
            name={`fields.${index}.required`}
            control={control}
            render={({ field: formField }) => (
              <div className="flex items-center gap-2 pb-2">
                <Checkbox
                  id={`field-required-${field.id}`}
                  checked={formField.value}
                  onCheckedChange={(checked) =>
                    formField.onChange(Boolean(checked))
                  }
                />
                <label
                  htmlFor={`field-required-${field.id}`}
                  className="text-xs text-muted-foreground"
                >
                  {t('form.fields.fields.required')}
                </label>
              </div>
            )}
          />

          <Button
            type="button"
            size="icon"
            variant="ghost"
            onClick={() => remove(index)}
          >
            <TrashIcon />
          </Button>
        </div>
      ))}
    </FieldGroup>
  );
}
