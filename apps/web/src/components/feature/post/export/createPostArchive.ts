import type { JSONContent } from '@tiptap/react';
import { downloadZip } from 'client-zip';

import type {
  GetPostResponse,
  GetPostResponseContentMediaItem,
} from '@/api/__generated__/types';

import { NodeType } from '../editor/extensions/nodes';
import { type MarkdownAsset, serializeToMarkdown } from './serializeToMarkdown';

const MARKDOWN_FILE_NAME = 'post.md';
const ASSETS_DIRECTORY = 'assets';

const MAX_FOLDER_NAME_LENGTH = 80;
const FALLBACK_FOLDER_NAME = 'post';

export class PostContentUnavailableError extends Error {}

export interface PostArchive {
  blob: Blob;
  fileName: string;
  /** 프리사인 URL 만료 등으로 내려받지 못한 첨부 수 */
  failedAssetCount: number;
}

/**
 * 게시물 하나를 마크다운 1개 + 첨부 자산으로 구성된 zip으로 만든다.
 *
 * ```
 * {slug}/post.md          -> ![](./assets/1042-diagram.png)
 * {slug}/assets/1042-diagram.png
 * ```
 */
export async function createPostArchive({
  post,
  url,
  signal,
}: {
  post: GetPostResponse;
  url: string;
  signal?: AbortSignal;
}): Promise<PostArchive> {
  const { summary, content } = post;

  if (content === undefined) {
    throw new PostContentUnavailableError();
  }

  const doc = parseContent(content.json);
  const { assets, assetsByMediaId, failedMediaIds } = await fetchAssets({
    doc,
    media: content.media,
    signal,
  });

  const markdown = buildMarkdown({
    summary,
    url,
    doc,
    resolveAsset: (mediaId) => assetsByMediaId.get(mediaId) ?? null,
  });

  const folder = toFolderName(summary.slug);
  const blob = await downloadZip([
    { name: `${folder}/${MARKDOWN_FILE_NAME}`, input: markdown },
    ...assets.map((asset) => ({
      name: `${folder}/${asset.path.replace(/^\.\//, '')}`,
      input: asset.blob,
    })),
  ]).blob();

  return {
    blob,
    fileName: `${folder}.zip`,
    failedAssetCount: failedMediaIds.length,
  };
}

function parseContent(json: string): JSONContent {
  try {
    return JSON.parse(json) as JSONContent;
  } catch {
    throw new Error('Failed to parse post content');
  }
}

function toFolderName(slug: string): string {
  const sanitized = slug
    .replace(/[ -<>:"/\\|?*]/g, '-')
    .replace(/\s+/g, '-')
    .replace(/-{2,}/g, '-')
    .replace(/^[.-]+|[.-]+$/g, '')
    .slice(0, MAX_FOLDER_NAME_LENGTH);

  return sanitized.length > 0 ? sanitized : FALLBACK_FOLDER_NAME;
}

// ---- 자산 내려받기 ----

const FETCH_CONCURRENCY = 6;
const FALLBACK_EXTENSION = 'bin';
const MAX_ASSET_NAME_LENGTH = 80;

const MIME_EXTENSIONS: Record<string, string> = {
  'image/apng': 'apng',
  'image/avif': 'avif',
  'image/bmp': 'bmp',
  'image/gif': 'gif',
  'image/heic': 'heic',
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/svg+xml': 'svg',
  'image/tiff': 'tiff',
  'image/webp': 'webp',
};

interface PostAsset {
  path: string;
  blob: Blob;
}

/**
 * 프리사인 URL은 10분 뒤 만료되므로(`MediaDomainService.generatePresignedGetUrl`)
 * 내보내기 시점에 바이트를 받아 zip에 함께 담는다. 일부가 실패해도 나머지는 그대로
 * 내보내고, 실패한 mediaId는 호출부가 사용자에게 알릴 수 있도록 돌려준다.
 */
async function fetchAssets({
  doc,
  media,
  signal,
}: {
  doc: JSONContent;
  media: GetPostResponseContentMediaItem[];
  signal?: AbortSignal;
}): Promise<{
  assets: PostAsset[];
  assetsByMediaId: Map<string, MarkdownAsset>;
  failedMediaIds: string[];
}> {
  const mediaById = new Map(media.map((item) => [String(item.id), item]));
  const mediaIds = collectReferencedMediaIds(doc).filter((mediaId) =>
    mediaById.has(mediaId),
  );

  const assets: PostAsset[] = [];
  const assetsByMediaId = new Map<string, MarkdownAsset>();
  const failedMediaIds: string[] = [];

  await mapWithConcurrency(mediaIds, FETCH_CONCURRENCY, async (mediaId) => {
    const { url, fileName } = mediaById.get(
      mediaId,
    ) as GetPostResponseContentMediaItem;

    try {
      const response = await fetch(url, { signal });

      if (!response.ok) {
        throw new Error(`Failed to download media ${mediaId}`);
      }

      const blob = await response.blob();
      const path = assetPath(mediaId, fileName, extensionOf(blob.type, url));

      assets.push({ path, blob });
      assetsByMediaId.set(mediaId, { path, fileName });
    } catch (error) {
      if (signal?.aborted) {
        throw error;
      }

      // 실패한 자산도 같은 경로를 가리켜, 빠진 파일이 드러나게 한다.
      failedMediaIds.push(mediaId);
      assetsByMediaId.set(mediaId, {
        path: assetPath(mediaId, fileName, extensionOf(null, url)),
        fileName,
      });
    }
  });

  return { assets, assetsByMediaId, failedMediaIds };
}

/**
 * 본문에서 실제로 참조하는 mediaId를 등장 순서대로 모은다. 첨부됐지만 본문에서
 * 지워진 미디어를 내려받지 않기 위한 것.
 */
function collectReferencedMediaIds(doc: JSONContent): string[] {
  const ids: string[] = [];
  const seen = new Set<string>();

  const walk = (node: JSONContent) => {
    if (node.type === NodeType.Image || node.type === NodeType.File) {
      const mediaId = node.attrs?.mediaId;

      if (typeof mediaId === 'string' || typeof mediaId === 'number') {
        const id = String(mediaId);

        if (!seen.has(id)) {
          seen.add(id);
          ids.push(id);
        }
      }
    }

    for (const child of node.content ?? []) {
      walk(child);
    }
  };

  walk(doc);

  return ids;
}

/**
 * mediaId를 앞에 붙여 이름이 겹치지 않게 하면서, 압축을 풀었을 때 알아볼 수 있도록
 * 원본 파일 이름을 함께 남긴다.
 */
function assetPath(
  mediaId: string,
  fileName: string | null,
  extension: string,
): string {
  const sanitized = (fileName ?? '')
    .replace(/[^\p{L}\p{N}._-]+/gu, '-')
    .replace(/-{2,}/g, '-')
    .replace(/^[.-]+|[.-]+$/g, '')
    .slice(0, MAX_ASSET_NAME_LENGTH);

  const baseName =
    sanitized.length === 0
      ? `asset.${extension}`
      : /\.[a-z0-9]{1,8}$/i.test(sanitized)
        ? sanitized
        : `${sanitized}.${extension}`;

  return `./${ASSETS_DIRECTORY}/${mediaId}-${baseName}`;
}

function extensionOf(contentType: string | null, url: string): string {
  const normalized = contentType?.split(';')[0]?.trim().toLowerCase();

  return (
    (normalized ? MIME_EXTENSIONS[normalized] : undefined) ??
    extensionFromUrl(url) ??
    FALLBACK_EXTENSION
  );
}

function extensionFromUrl(url: string): string | undefined {
  try {
    const { pathname } = new URL(url, 'https://placeholder.invalid');
    const fileName = pathname.split('/').pop() ?? '';

    if (!fileName.includes('.')) {
      return undefined;
    }

    const extension = fileName.split('.').pop() as string;

    return /^[a-z0-9]{1,8}$/i.test(extension)
      ? extension.toLowerCase()
      : undefined;
  } catch {
    return undefined;
  }
}

async function mapWithConcurrency<T>(
  items: T[],
  limit: number,
  task: (item: T) => Promise<void>,
): Promise<void> {
  let cursor = 0;

  const workers = Array.from(
    { length: Math.min(limit, items.length) },
    async () => {
      while (cursor < items.length) {
        const item = items[cursor];
        cursor += 1;

        if (item !== undefined) {
          await task(item);
        }
      }
    },
  );

  await Promise.all(workers);
}

// ---- 마크다운 문서 조립 ----

function buildMarkdown({
  summary,
  url,
  doc,
  resolveAsset,
}: {
  summary: GetPostResponse['summary'];
  url: string;
  doc: JSONContent;
  resolveAsset: (mediaId: string) => MarkdownAsset | null;
}): string {
  const title = summary.title?.trim();

  const sections = [
    buildFrontMatter(summary, url),
    title ? `# ${title}` : '',
    serializeToMarkdown(doc, { resolveAsset }).trimEnd(),
  ].filter((section) => section.length > 0);

  return `${sections.join('\n\n')}\n`;
}

function buildFrontMatter(
  summary: GetPostResponse['summary'],
  url: string,
): string {
  const title = summary.title?.trim();
  const lines = ['---'];

  if (title) {
    lines.push(`title: ${yamlScalar(title)}`);
  }

  lines.push(`slug: ${yamlScalar(summary.slug)}`);
  lines.push(`type: ${yamlScalar(summary.type)}`);
  lines.push(`url: ${yamlScalar(url)}`);
  lines.push(
    `author: ${yamlScalar(`${summary.author.name} (@${summary.author.handle})`)}`,
  );

  const projectName = summary.project?.name?.trim();
  const projectHandle = summary.project?.handle?.trim();

  if (projectName || projectHandle) {
    lines.push(
      `project: ${yamlScalar(
        projectHandle
          ? `${projectName ?? projectHandle} (@${projectHandle})`
          : (projectName as string),
      )}`,
    );
  }

  lines.push(`createdAt: ${yamlScalar(summary.createdAt)}`);

  const tags = summary.tags
    .map((tag) => tag.name.trim())
    .filter((name) => name.length > 0);

  if (tags.length > 0) {
    lines.push('tags:');
    lines.push(...tags.map((name) => `  - ${yamlScalar(name)}`));
  }

  lines.push('---');

  return lines.join('\n');
}

/**
 * 평범한 문자열만 그대로 두고, YAML에서 다르게 해석될 수 있는 값은 모두 단일
 * 인용부호로 감싼다.
 */
function yamlScalar(value: string): string {
  const isPlainSafe =
    /^[\p{L}\p{N}][\p{L}\p{N} ._/-]*$/u.test(value) &&
    !/^(?:true|false|null|yes|no|on|off)$/i.test(value) &&
    Number.isNaN(Number(value)) &&
    value === value.trim();

  return isPlainSafe ? value : `'${value.replace(/'/g, "''")}'`;
}
