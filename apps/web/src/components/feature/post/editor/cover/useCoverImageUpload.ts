import { useState } from 'react';

import { useUploadMedia } from '@/api/__generated__/media/media';
import { uploadFileToS3 } from '@/api/s3';
import SyncError, { ErrorCode } from '@/lib/error';

export type CoverUploadResult =
  | { ok: true; mediaId: string }
  | { ok: false; code?: ErrorCode };

/**
 * Uploads a generated cover image via the standard 2-step presigned-PUT flow
 * (`POST /media` → PUT to S3), the same path used for editor images and profile
 * pictures. Returns the resulting `mediaId`; the caller links it to the post on
 * save by sending it as `coverMediaId`. Does not touch any post mutation — the
 * post may not exist yet at generation time.
 */
export function useCoverImageUpload() {
  const { mutateAsync: uploadMedia } = useUploadMedia();
  const [isUploading, setIsUploading] = useState(false);

  async function upload(file: File): Promise<CoverUploadResult> {
    setIsUploading(true);
    try {
      const {
        data: { uploadUrl, mediaId, contentType },
      } = await uploadMedia({
        data: {
          fileName: file.name,
          fileSize: file.size,
          mediaType: file.type,
        },
      });

      const { success } = await uploadFileToS3({
        file,
        uploadUrl,
        contentType,
      });
      if (!success) return { ok: false };
      return { ok: true, mediaId };
    } catch (error) {
      if (error instanceof SyncError) {
        return { ok: false, code: error.code };
      }
      return { ok: false };
    } finally {
      setIsUploading(false);
    }
  }

  return { upload, isUploading };
}
