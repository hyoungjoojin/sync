import ky from 'ky';

interface S3UploadResponse {
  success: boolean;
}

/**
 * `POST /media`가 발급한 presigned URL로 파일을 올린다.
 *
 * `contentType`은 응답의 `contentType`을 그대로 넘겨야 한다. 서버가 이 값을 서명에
 * 포함시키기 때문에 `file.type` 등 다른 값을 보내면 S3가 403으로 거부한다.
 */
export async function uploadFileToS3({
  uploadUrl,
  file,
  contentType,
}: {
  uploadUrl: string;
  file: File;
  contentType: string;
}): Promise<S3UploadResponse> {
  return ky
    .put<void>(uploadUrl, {
      headers: {
        'Content-Type': contentType,
      },
      body: file,
    })
    .then(() => ({
      success: true,
    }))
    .catch(() => ({
      success: false,
    }));
}
