'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useMemo } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import { useChangePassword } from '@/api/__generated__/auth/auth';
import { useGetAuthenticatedUser } from '@/api/__generated__/profile/profile';
import { Button } from '@/components/ui/button';
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import SyncError, { ErrorCode } from '@/lib/error';

const MIN_PASSWORD_LENGTH = 8;

const PasswordSettingsFormSchema = (
  t: ReturnType<typeof useTranslations>,
  hasPassword: boolean,
) =>
  z
    .object({
      currentPassword: z.string(),
      newPassword: z
        .string()
        .min(1, {
          error: t('form.errors.required_new_password'),
        })
        .min(MIN_PASSWORD_LENGTH, {
          error: t('form.errors.password_min_length', {
            length: MIN_PASSWORD_LENGTH,
          }),
        }),
      confirmPassword: z.string().min(1, {
        error: t('form.errors.required_confirm_password'),
      }),
    })
    .superRefine((val, ctx) => {
      if (hasPassword && val.currentPassword.length === 0) {
        ctx.addIssue({
          code: 'custom',
          message: t('form.errors.required_current_password'),
          path: ['currentPassword'],
        });
      }

      if (val.newPassword !== val.confirmPassword) {
        ctx.addIssue({
          code: 'custom',
          message: t('form.errors.passwords_must_match'),
          path: ['confirmPassword'],
        });
      }
    });

type PasswordSettingsFormValues = z.infer<
  ReturnType<typeof PasswordSettingsFormSchema>
>;

const emptyFormValues: PasswordSettingsFormValues = {
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
};

export default function PasswordSettings() {
  const t = useTranslations('modals.settings.categories.account.password');

  const { data: profile } = useGetAuthenticatedUser();

  const hasPassword = profile?.data.hasPassword ?? true;

  const schema = useMemo(
    () => PasswordSettingsFormSchema(t, hasPassword),
    [t, hasPassword],
  );

  const form = useForm<PasswordSettingsFormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyFormValues,
  });

  const { mutate: changePassword, isPending } = useChangePassword();

  const onFormSubmit = (values: PasswordSettingsFormValues) => {
    changePassword(
      {
        data: {
          currentPassword: hasPassword ? values.currentPassword : null,
          newPassword: values.newPassword,
        },
      },
      {
        onSuccess: () => {
          form.reset(emptyFormValues);
          toast.success(
            hasPassword ? t('messages.success') : t('messages.set_success'),
          );
        },
        onError: (error) => {
          if (
            error instanceof SyncError &&
            error.code === ErrorCode.INVALID_CURRENT_PASSWORD
          ) {
            form.setError('currentPassword', {
              type: 'server',
              message: t('form.errors.invalid_current_password'),
            });
            return;
          }

          toast.error(t('form.errors.change_failed'));
        },
      },
    );
  };

  return (
    <div>
      <h2 className="font-bold">{hasPassword ? t('title') : t('set.title')}</h2>
      <p className="text-xs mb-4">
        {hasPassword ? t('description') : t('set.description')}
      </p>

      <form onSubmit={form.handleSubmit(onFormSubmit)}>
        <FieldGroup className="p-3">
          {hasPassword && (
            <Controller
              name="currentPassword"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <div className="flex items-center justify-between">
                    <FieldLabel htmlFor="password-settings-current">
                      {t('form.fields.current_password.label')}
                    </FieldLabel>

                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} />
                    )}
                  </div>

                  <Input
                    {...field}
                    id="password-settings-current"
                    type="password"
                    aria-invalid={fieldState.invalid}
                    placeholder={t('form.fields.current_password.placeholder')}
                    autoComplete="current-password"
                  />
                </Field>
              )}
            />
          )}

          <Controller
            name="newPassword"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <div className="flex items-center justify-between">
                  <FieldLabel htmlFor="password-settings-new">
                    {t('form.fields.new_password.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="password-settings-new"
                  type="password"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('form.fields.new_password.placeholder')}
                  autoComplete="new-password"
                />
              </Field>
            )}
          />

          <Controller
            name="confirmPassword"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <div className="flex items-center justify-between">
                  <FieldLabel htmlFor="password-settings-confirm">
                    {t('form.fields.confirm_password.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="password-settings-confirm"
                  type="password"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('form.fields.confirm_password.placeholder')}
                  autoComplete="new-password"
                />
              </Field>
            )}
          />

          <div className="flex justify-end">
            <Button type="submit" disabled={isPending}>
              {hasPassword
                ? t('form.submit.label')
                : t('form.submit.set_label')}
            </Button>
          </div>
        </FieldGroup>
      </form>
    </div>
  );
}
