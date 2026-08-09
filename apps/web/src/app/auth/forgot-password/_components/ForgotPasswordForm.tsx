'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { useRequestPasswordReset } from '@/api/__generated__/auth/auth';
import { Button, LinkButton } from '@/components/ui/button';
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import SyncError, { ErrorCode } from '@/lib/error';
import ROUTES from '@/util/routes';

export default function ForgotPasswordForm() {
  const t = useTranslations('pages.forgot_password');

  const [sentTo, setSentTo] = useState<string | null>(null);

  const { mutate: requestPasswordReset, isPending } = useRequestPasswordReset();

  const ForgotPasswordFormSchema = z.object({
    email: z
      .email({
        error: t('form.errors.invalid_email'),
      })
      .nonoptional({
        error: t('form.errors.required_email'),
      }),
  });

  type ForgotPasswordFormValues = z.infer<typeof ForgotPasswordFormSchema>;

  const form = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(ForgotPasswordFormSchema),
    defaultValues: {
      email: '',
    },
  });

  const onFormSubmit = async (values: ForgotPasswordFormValues) => {
    requestPasswordReset(
      {
        data: {
          email: values.email,
        },
      },
      {
        onSuccess: () => {
          setSentTo(values.email);
        },
        onError: (error) => {
          if (
            error instanceof SyncError &&
            error.code === ErrorCode.USER_NOT_FOUND
          ) {
            form.setError('email', {
              type: 'server',
              message: t('form.errors.account_not_found'),
            });
            return;
          }

          toast.error(t('form.errors.request_failed'));
        },
      },
    );
  };

  if (sentTo !== null) {
    return (
      <div className="flex flex-col gap-4">
        <div>
          <h2 className="text-lg font-medium mb-2">{t('sent.title')}</h2>
          <p className="text-muted-foreground">
            {t('sent.description', { email: sentTo })}
          </p>
        </div>

        <p className="text-sm text-muted-foreground">{t('sent.notice')}</p>

        <LinkButton className="w-full" variant="link" href={ROUTES.LOGIN()}>
          {t('form.links.login.label')}
        </LinkButton>
      </div>
    );
  }

  return (
    <div>
      <form onSubmit={form.handleSubmit(onFormSubmit)}>
        <FieldGroup>
          <Controller
            name="email"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <div className="flex items-center justify-between">
                  <FieldLabel htmlFor="forgot-password-form-email">
                    {t('form.email.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="forgot-password-form-email"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('form.email.placeholder')}
                  autoComplete="email"
                />
              </Field>
            )}
          />

          <div>
            <Button className="w-full" type="submit" disabled={isPending}>
              {t('form.submit.label')}
            </Button>

            <LinkButton className="w-full" variant="link" href={ROUTES.LOGIN()}>
              {t('form.links.login.label')}
            </LinkButton>
          </div>
        </FieldGroup>
      </form>
    </div>
  );
}
