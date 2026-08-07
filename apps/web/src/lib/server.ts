import ky, { HTTPError } from 'ky';

import koMessages from '@/public/locales/ko.json';
import ROUTES from '@/util/routes';
import { getCookies, getCsrfToken, isServer } from '@/util/server';

import { env } from './env';
import SyncError, { ErrorCode } from './error';
import { getQueryClient } from './query';

interface ErrorResponse {
  detail: string;
  instance: string;
  status: number;
  title: string;
  code: ErrorCode;
}

// 정적 import는 generated 클라이언트 -> 이 모듈(api mutator) -> generated 클라이언트
// 순환이 되므로, 쿼리 키만 필요한 이 경로에서는 동적 import로 끊는다.
async function invalidateClientSessionIfAuthenticated() {
  const { getGetAuthenticatedUserQueryKey } =
    await import('@/api/__generated__/profile/profile');

  const queryClient = getQueryClient();

  // 로그인 상태였던 적이 없으면(익명 사용자의 401) 아무것도 하지 않는다.
  if (
    queryClient.getQueryData(getGetAuthenticatedUserQueryKey()) === undefined
  ) {
    return;
  }

  queryClient.clear();
  window.location.href = ROUTES.LOGIN();
}

export const server = ky.extend({
  prefixUrl: env.NEXT_PUBLIC_BACKEND_URL,
  credentials: 'include',
  retry: {
    limit: 1,
    methods: ['post', 'put', 'patch', 'delete'],
    statusCodes: [403],
  },
  hooks: {
    beforeRequest: [
      async (request) => {
        if (isServer()) {
          const cookies = await getCookies();
          if (cookies) {
            request.headers.set('Cookie', cookies);
          }
        }
      },
      async (request) => {
        if (request.method !== 'GET' && request.method !== 'HEAD') {
          const csrfToken = await getCsrfToken();
          if (csrfToken) {
            request.headers.set('X-XSRF-TOKEN', csrfToken);
          }
        }
      },
    ],
    beforeError: [
      async (error) => {
        const { response } = error;

        if (response.status === 401) {
          if (!isServer()) {
            await invalidateClientSessionIfAuthenticated();
          }

          return error;
        }

        // 403은 SyncException(도메인 규칙 위반)과 CSRF/AccessDenied 거부
        // 둘 다에서 발생할 수 있다. 후자는 GlobalExceptionHandler를 거치지
        // 않아 본문에 code 필드가 없으므로, 상태 코드가 아니라 code 필드의
        // 유무로 둘을 구분한다.
        const body = await response
          .json<Partial<ErrorResponse>>()
          .catch(() => null);
        const code = body?.code;

        if (code && (Object.values(ErrorCode) as string[]).includes(code)) {
          throw new SyncError(body?.detail ?? '', code);
        }

        return error;
      },
    ],
  },
});

export async function primeCsrfToken() {
  try {
    await server.get('auth/csrf');
  } catch {
    // Best-effort — worst case the CSRF cookie stays unset until the next GET.
  }
}

const getUrl = (url: string) => {
  if (url.startsWith('/')) {
    return url.slice(1);
  }

  return url;
};

export const api = async <T>(url: string, options: RequestInit): Promise<T> => {
  let response: Response;
  try {
    response = await server(getUrl(url), options);
  } catch (error) {
    if (error instanceof SyncError) {
      throw error;
    }

    // beforeError가 401을 HTTPError 그대로 흘려보내므로, 여기서 미인증을
    // 네트워크 오류와 구분되는 코드로 변환한다. getSession이 이 코드를 본다.
    if (error instanceof HTTPError && error.response.status === 401) {
      throw new SyncError(
        koMessages.errors['connection-failed'],
        ErrorCode.UNAUTHORIZED,
      );
    }

    throw new SyncError(
      koMessages.errors['connection-failed'],
      ErrorCode.NETWORK_ERROR,
    );
  }

  if (response.status === 204) {
    return {
      status: response.status,
      data: undefined,
      headers: response.headers,
    } as T;
  }

  const contentType = response.headers.get('Content-Type');

  let data;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  return {
    status: response.status,
    data,
    headers: response.headers,
  } as T;
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export type ErrorType<_T> = SyncError;
