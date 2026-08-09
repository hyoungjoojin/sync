import { getPostBySlug } from '@/api/__generated__/post/post';

async function resolveFreshUrl(slug: string, mediaId: string): Promise<string> {
  const { data: post } = await getPostBySlug(slug);
  const url = post.content?.media.find(
    (item) => String(item.id) === mediaId,
  )?.url;

  if (!url) {
    throw new Error(`Media ${mediaId} is no longer attached to ${slug}`);
  }

  return url;
}

/**
 * 미디어 바이트를 가져온다. 본문에 실려 온 URL은 10분짜리 프리사인 URL이라 오래
 * 열어둔 화면에서는 이미 만료됐을 수 있으므로, 실패하면 본문을 다시 받아 새 URL로
 * 한 번 더 시도한다. 아직 저장되지 않은 글에서는 `slug` 가 없어 재시도할 수 없다.
 */
export async function fetchMediaBlob({
  mediaId,
  url,
  slug,
  signal,
}: {
  mediaId: string;
  url: string | null;
  slug: string | null;
  signal?: AbortSignal;
}): Promise<Blob> {
  if (url) {
    const response = await fetch(url, { signal }).catch(() => null);

    if (response?.ok) {
      return response.blob();
    }

    if (signal?.aborted) {
      throw new Error(`Fetching media ${mediaId} was aborted`);
    }
  }

  if (!slug) {
    throw new Error(
      `Cannot refresh the URL for media ${mediaId} without a slug`,
    );
  }

  const response = await fetch(await resolveFreshUrl(slug, mediaId), {
    signal,
  });

  if (!response.ok) {
    throw new Error(`Failed to download media ${mediaId}`);
  }

  return response.blob();
}
