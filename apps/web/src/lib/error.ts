export enum ErrorCode {
  OAUTH2_ACCOUNT_CANNOT_BE_DELETED = 'OAUTH2_ACCOUNT_CANNOT_BE_DELETED',
  USER_NOT_FOUND = 'USER_NOT_FOUND',
  USER_ALREADY_EXISTS = 'USER_ALREADY_EXISTS',
  PROVIDER_NOT_FOUND = 'PROVIDER_NOT_FOUND',
  MEDIA_NOT_FOUND = 'MEDIA_NOT_FOUND',
  MEDIA_UPLOAD_FAILED = 'MEDIA_UPLOAD_FAILED',
  MESSAGE_TO_SELF = 'MESSAGE_TO_SELF',
  PROJECT_NOT_FOUND = 'PROJECT_NOT_FOUND',
  PROJECT_HANDLE_ALREADY_EXISTS = 'PROJECT_HANDLE_ALREADY_EXISTS',
  POST_NOT_FOUND = 'POST_NOT_FOUND',
  TAG_NOT_FOUND = 'TAG_NOT_FOUND',
  NETWORK_ERROR = 'NETWORK_ERROR',
  UNAUTHORIZED = 'UNAUTHORIZED',
  EMAIL_NOT_VERIFIED = 'EMAIL_NOT_VERIFIED',
  EMAIL_ALREADY_VERIFIED = 'EMAIL_ALREADY_VERIFIED',
  INVALID_PASSWORD_RESET_TOKEN = 'INVALID_PASSWORD_RESET_TOKEN',
  EXPIRED_PASSWORD_RESET_TOKEN = 'EXPIRED_PASSWORD_RESET_TOKEN',
  INVALID_CURRENT_PASSWORD = 'INVALID_CURRENT_PASSWORD',
  HANDLE_NOT_SET = 'HANDLE_NOT_SET',
  CAPTCHA_VERIFICATION_FAILED = 'CAPTCHA_VERIFICATION_FAILED',
}

const DIGEST_PREFIX = 'SYNC_';

export default class SyncError extends Error {
  public readonly code: ErrorCode;

  /**
   * 서버 컴포넌트에서 던진 에러는 프로덕션에서 message가 지워진 채
   * digest만 error.tsx로 전달된다. Next.js는 이미 digest가 있으면 그 값을
   * 그대로 보존하므로(create-error-handler), 에러 화면이 원인을 구분할 수
   * 있도록 코드를 digest에 실어 보낸다.
   */
  public readonly digest: string;

  constructor(message: string, code: ErrorCode) {
    super(message);

    Object.setPrototypeOf(this, SyncError.prototype);
    Error.captureStackTrace(this, SyncError);

    this.code = code;
    this.digest = `${DIGEST_PREFIX}${code}`;
  }
}

export function getErrorCodeFromDigest(digest?: string): ErrorCode | null {
  if (digest === undefined || !digest.startsWith(DIGEST_PREFIX)) {
    return null;
  }

  const code = digest.slice(DIGEST_PREFIX.length);

  return (
    Object.values(ErrorCode).find((errorCode) => errorCode === code) ?? null
  );
}
