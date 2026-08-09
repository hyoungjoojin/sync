'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { useLogin } from '@/api/__generated__/auth/auth';
import { Button, LinkButton } from '@/components/ui/button';
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { useSession } from '@/lib/auth/client';
import SyncError, { ErrorCode } from '@/lib/error';
import ROUTES from '@/util/routes';

interface LoginFormProps {
  onSuccess?: () => void;
  redirectTo?: string;
}

export default function LoginForm({ onSuccess, redirectTo }: LoginFormProps) {
  const t = useTranslations('pages.login.form');

  const router = useRouter();
  const { mutate: login } = useLogin();

  const { refetch: refetchSession } = useSession();

  const LoginFormSchema = z.object({
    email: z
      .email({
        error: t('errors.invalid_credentials'),
      })
      .nonoptional({
        error: t('errors.required_email'),
      }),
    password: z.string().nonempty({
      error: t('errors.required_password'),
    }),
  });

  type LoginFormValues = z.infer<typeof LoginFormSchema>;

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(LoginFormSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onFormSubmit = async (values: LoginFormValues) => {
    login(
      {
        data: {
          email: values.email,
          password: values.password,
        },
      },
      {
        onSuccess: async () => {
          await refetchSession();

          if (onSuccess) {
            onSuccess();
            return;
          }

          // redirectTo 는 에이전트 OAuth 흐름이 되돌아갈 /oauth2/authorize 처럼 Next 가 아니라
          // 서버가 처리하는 경로일 수 있다. 클라이언트 라우터로는 그런 경로를 열 수 없으므로
          // 통째로 이동한다.
          if (redirectTo) {
            window.location.replace(redirectTo);
            return;
          }

          router.replace(ROUTES.HOME());
        },
        onError: (error) => {
          if (
            error instanceof SyncError &&
            error.code === ErrorCode.NETWORK_ERROR
          ) {
            toast.error(t('errors.network'));
            return;
          }

          toast.error(t('errors.invalid-credentials'));
        },
      },
    );
  };

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
                  <FieldLabel htmlFor="login-form-email">
                    {t('email.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="login-form-email"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('email.placeholder')}
                  autoComplete="email"
                />
              </Field>
            )}
          />
          <Controller
            name="password"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <div className="flex items-center justify-between">
                  <FieldLabel htmlFor="login-form-password">
                    {t('password.label')}
                  </FieldLabel>

                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </div>

                <Input
                  {...field}
                  id="login-form-password"
                  type="password"
                  aria-invalid={fieldState.invalid}
                  placeholder={t('password.placeholder')}
                  autoComplete="current-password"
                />
              </Field>
            )}
          />

          <div>
            <Button className="w-full" type="submit">
              {t('submit.label')}
            </Button>

            <LinkButton
              className="w-full"
              variant="link"
              href={ROUTES.FORGOT_PASSWORD()}
            >
              {t('links.forgot_password.label')}
            </LinkButton>

            <LinkButton
              className="w-full"
              variant="link"
              href={ROUTES.REGISTER()}
            >
              {t('links.register.label')}
            </LinkButton>
          </div>
        </FieldGroup>
      </form>
    </div>
  );
}
