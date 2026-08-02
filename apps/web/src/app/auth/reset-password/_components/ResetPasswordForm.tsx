'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { useConfirmPasswordReset } from '@/api/__generated__/auth/auth';
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

import InvalidResetLink, {
  RESET_LINK_REASON,
  type ResetLinkReason,
} from './InvalidResetLink';

const MIN_PASSWORD_LENGTH = 8;

interface ResetPasswordFormProps {
  token: string;
}

export default function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const t = useTranslations('pages.reset_password');

  const router = useRouter();

  const [linkError, setLinkError] = useState<ResetLinkReason | null>(null);

  const { mutate: confirmPasswordReset, isPending } = useConfirmPasswordReset();

  const ResetPasswordFormSchema = z
    .object({
      password: z
        .string()
        .nonempty({
          error: t('form.errors.required_password'),
        })
        .min(MIN_PASSWORD_LENGTH, {
          error: t('form.errors.password_min_length', {
            length: MIN_PASSWORD_LENGTH,
          }),
        }),
      confirmPassword: z.string().nonempty({
        error: t('form.errors.required_confirm_password'),
      }),
    })
    .superRefine((val, ctx) => {
      if (val.password !== val.confirmPassword) {
        ctx.addIssue({
          code: 'custom',
          message: t('form.errors.passwords_must_match'),
          path: ['confirmPassword'],
        });
      }
    });

  type ResetPasswordFormValues = z.infer<typeof ResetPasswordFormSchema>;

  const form = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(ResetPasswordFormSchema),
    defaultValues: {
      password: '',
      confirmPassword: '',
    },
  });

  const onFormSubmit = async (values: ResetPasswordFormValues) => {
    confirmPasswordReset(
      {
        data: {
          token,
          newPassword: values.password,
        },
      },
      {
        onSuccess: () => {
          toast.success(t('success'));
          router.replace(ROUTES.LOGIN());
        },
        onError: (error) => {
          if (error instanceof SyncError) {
            const { code } = error;

            if (code === ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN) {
              setLinkError(RESET_LINK_REASON.EXPIRED);
              return;
            }

            if (code === ErrorCode.INVALID_PASSWORD_RESET_TOKEN) {
              setLinkError(RESET_LINK_REASON.INVALID);
              return;
            }
          }

          toast.error(t('form.errors.reset_failed'));
        },
      },
    );
  };

  if (linkError !== null) {
    return <InvalidResetLink reason={linkError} />;
  }

  return (
    <div>
      <form onSubmit={form.handleSubmit(onFormSubmit)}>
        <FieldGroup>
          <Controller
            name="password"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <div className="flex items-center justify-between">
                  <FieldLabel htmlFor="reset-password-form-password">
                    {t('form.password.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="reset-password-form-password"
                  type="password"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('form.password.placeholder')}
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
                  <FieldLabel htmlFor="reset-password-form-confirm-password">
                    {t('form.confirm_password.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="reset-password-form-confirm-password"
                  type="password"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('form.confirm_password.placeholder')}
                  autoComplete="new-password"
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
