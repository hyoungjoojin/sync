import {
  ArrowCounterClockwiseIcon,
  DownloadSimpleIcon,
  EyeIcon,
  EyeSlashIcon,
  PaperclipIcon,
} from '@phosphor-icons/react';
import {
  Node,
  NodeViewProps,
  NodeViewWrapper,
  ReactNodeViewRenderer,
  mergeAttributes,
} from '@tiptap/react';
import { useTranslations } from 'next-intl';
import { useCallback, useEffect } from 'react';
import { toast } from 'sonner';

import { useUploadMedia } from '@/api/__generated__/media/media';
import { uploadFileToS3 } from '@/api/s3';
import { useDownloadPostMedia } from '@/components/feature/post/hooks/useDownloadPostMedia';
import { Button } from '@/components/ui/button';
import { FileInput } from '@/components/ui/input';
import { Spinner } from '@/components/ui/spinner';
import {
  ATTACHMENT_ACCEPT,
  MAX_ATTACHMENT_FILE_SIZE,
  cn,
  formatFileSize,
  isPreviewableMediaType,
} from '@/lib/tiptap-utils';

import { NodeType } from '..';
import {
  getLocalFile,
  hasSupportedFileTransfer,
  rememberLocalFile,
  takePendingFile,
} from '../../media-drop';
import { FilePreview } from './preview';

export type FileNodeAttributes = {
  mediaId: string | null;
  /** 본문에 미리보기를 펼친 채로 둘지. 작성자가 정하고 저장된다. */
  showPreview: boolean;
  status: 'none' | 'uploading' | 'uploaded' | 'error';
  url: string | null;
  fileName: string | null;
  fileSize: number | null;
  mediaType: string | null;
  pendingId: string | null;
};

export type FileNodeOptions = {
  /** 만료된 프리사인 URL을 새로 받아오기 위해 필요한 게시물 슬러그. */
  slug: string | null;
};

const fileNodeSchema = {
  name: NodeType.File,
  group: 'block',
  content: '',
  atom: true,
  selectable: true,
  draggable: true,
  addAttributes() {
    return {
      mediaId: {
        default: null,
        parseHTML: (element: Element) => element.getAttribute('data-media-id'),
        renderHTML: (attributes: Record<string, unknown>) =>
          attributes.mediaId ? { 'data-media-id': attributes.mediaId } : {},
      },
      showPreview: {
        default: false,
        parseHTML: (element: Element) =>
          element.getAttribute('data-show-preview') === 'true',
        renderHTML: (attributes: Record<string, unknown>) =>
          attributes.showPreview ? { 'data-show-preview': 'true' } : {},
      },
      status: {
        default: 'none',
        rendered: false,
      },
      url: {
        default: null,
        rendered: false,
      },
      fileName: {
        default: null,
        rendered: false,
      },
      fileSize: {
        default: null,
        rendered: false,
      },
      mediaType: {
        default: null,
        rendered: false,
      },
      pendingId: {
        default: null,
        rendered: false,
      },
    };
  },
  parseHTML() {
    return [
      {
        tag: 'div[data-media-id][data-node-type="file"]',
        getAttrs: (element: Element) => ({
          status: element.getAttribute('data-media-id') ? 'uploaded' : 'none',
        }),
      },
    ];
  },
  renderHTML({
    HTMLAttributes,
  }: {
    HTMLAttributes: Record<string, unknown>;
  }): [string, Record<string, unknown>] {
    return [
      'div',
      mergeAttributes(HTMLAttributes, { 'data-node-type': 'file' }),
    ];
  },
};

export const FileNode = Node.create<FileNodeOptions>({
  ...fileNodeSchema,
  addOptions() {
    return { slug: null };
  },
  addNodeView() {
    return ReactNodeViewRenderer(FileNodeComponent);
  },
});

export const ReadOnlyFileNode = Node.create<FileNodeOptions>({
  ...fileNodeSchema,
  addOptions() {
    return { slug: null };
  },
  addNodeView() {
    return ReactNodeViewRenderer(ReadOnlyFileNodeComponent);
  },
});

function FileNodeComponent({
  node,
  selected,
  extension,
  updateAttributes,
}: NodeViewProps) {
  const { mediaId, showPreview, status, url, fileName, fileSize, mediaType } =
    node.attrs as FileNodeAttributes;
  const { pendingId } = node.attrs as FileNodeAttributes;
  const { slug } = extension.options as FileNodeOptions;

  const t = useTranslations('components.editor.file');
  const { mutateAsync: uploadMedia } = useUploadMedia();

  const handleFileUpload = useCallback(
    (files: File[]) => {
      const file = files[0];
      if (!file) {
        return;
      }

      if (file.size > MAX_ATTACHMENT_FILE_SIZE) {
        toast.error(t('errors.max-size'));
        updateAttributes({ status: 'error', fileName: file.name });
        return;
      }

      updateAttributes({
        status: 'uploading',
        fileName: file.name,
        fileSize: file.size,
        mediaType: file.type,
      });

      uploadMedia({
        data: {
          fileName: file.name,
          fileSize: file.size,
          mediaType: file.type,
        },
      })
        .then(({ data: { mediaId, uploadUrl, contentType } }) => {
          // 저장 전에도 미리보기를 펼칠 수 있도록 원본을 붙잡아 둔다.
          rememberLocalFile(mediaId, file);
          updateAttributes({ mediaId });

          return uploadFileToS3({ uploadUrl, file, contentType });
        })
        .then(({ success }) => {
          if (!success) {
            throw new Error(t('errors.s3-upload-failed'));
          }

          updateAttributes({ status: 'uploaded' });
          toast.success(t('messages.upload-success'));
        })
        .catch((error) => {
          toast.error(
            error instanceof Error ? error.message : t('errors.upload-failed'),
          );

          updateAttributes({ status: 'error' });
        });
    },
    [t, updateAttributes, uploadMedia],
  );

  useEffect(() => {
    if (!pendingId) {
      return;
    }

    const file = takePendingFile(pendingId);

    // updateAttributes 는 ProseMirror 트랜잭션을 flushSync 로 반영하므로, 커밋 중인
    // 이펙트에서 바로 부르면 React 가 렌더 도중의 flushSync 라며 막는다.
    queueMicrotask(() => {
      updateAttributes({ pendingId: null });

      if (file) {
        handleFileUpload([file]);
      }
    });
  }, [pendingId, updateAttributes, handleFileUpload]);

  if (status === 'none') {
    return (
      <NodeViewWrapper className="my-6">
        <FileInput
          accept={ATTACHMENT_ACCEPT}
          onFileChange={handleFileUpload}
          maxFiles={1}
        >
          <div
            onDragEnter={(event) => {
              if (!hasSupportedFileTransfer(event.dataTransfer)) {
                event.preventDefault();
              }
            }}
            className={cn(
              'flex w-full items-center justify-center gap-2 rounded-lg bg-muted py-8 text-muted-foreground transition-colors hover:bg-muted/50 hover:text-foreground',
              selected && 'ring-2 ring-inset ring-primary/60',
            )}
          >
            <PaperclipIcon className="size-5" />
            <span className="text-sm font-medium">
              {t('placeholders.upload')}
            </span>
          </div>
        </FileInput>
      </NodeViewWrapper>
    );
  }

  const isPreviewable =
    status === 'uploaded' &&
    mediaId !== null &&
    isPreviewableMediaType(mediaType);

  return (
    <NodeViewWrapper className="my-6">
      <div
        className={cn(
          'w-full rounded-lg border bg-card',
          selected && 'ring-2 ring-inset ring-primary/60',
          status === 'error' && 'border-destructive',
        )}
      >
        <div className="flex items-center gap-3 px-4 py-3">
          <PaperclipIcon className="size-5 shrink-0 text-muted-foreground" />

          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">
              {fileName ?? t('placeholders.unknown-name')}
            </p>
            {status === 'error' ? (
              <p className="text-xs text-destructive">
                {t('errors.upload-failed')}
              </p>
            ) : (
              fileSize !== null && (
                <p className="text-xs text-muted-foreground">
                  {formatFileSize(fileSize)}
                </p>
              )
            )}
          </div>

          {status === 'uploading' && <Spinner className="size-4 shrink-0" />}

          {isPreviewable && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => updateAttributes({ showPreview: !showPreview })}
            >
              {showPreview ? (
                <EyeSlashIcon className="size-4" />
              ) : (
                <EyeIcon className="size-4" />
              )}
              {showPreview
                ? t('actions.hide-preview')
                : t('actions.show-preview')}
            </Button>
          )}

          {status === 'error' && (
            <FileInput
              accept={ATTACHMENT_ACCEPT}
              onFileChange={handleFileUpload}
              maxFiles={1}
            >
              <Button variant="outline" size="sm">
                <ArrowCounterClockwiseIcon className="size-4" />
                {t('actions.retry')}
              </Button>
            </FileInput>
          )}
        </div>

        {isPreviewable && showPreview && (
          <div className="border-t px-4 py-3">
            <FilePreview
              mediaId={mediaId}
              mediaType={mediaType as string}
              url={url}
              slug={slug}
              localFile={getLocalFile(mediaId)}
            />
          </div>
        )}
      </div>
    </NodeViewWrapper>
  );
}

function ReadOnlyFileNodeComponent({ node, extension }: NodeViewProps) {
  const { mediaId, showPreview, url, fileName, fileSize, mediaType } =
    node.attrs as FileNodeAttributes;
  const { slug } = extension.options as FileNodeOptions;

  const t = useTranslations('components.editor.file');
  const { download, downloadingMediaId } = useDownloadPostMedia(slug);

  const isDownloading = mediaId !== null && downloadingMediaId === mediaId;
  const isPreviewOpen =
    showPreview && mediaId !== null && isPreviewableMediaType(mediaType);

  return (
    <NodeViewWrapper className="my-6">
      <div className="w-full rounded-lg border bg-card">
        <div className="flex items-center gap-3 px-4 py-3">
          <PaperclipIcon className="size-5 shrink-0 text-muted-foreground" />

          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">
              {fileName ?? t('placeholders.unknown-name')}
            </p>
            {fileSize !== null && (
              <p className="text-xs text-muted-foreground">
                {formatFileSize(fileSize)}
              </p>
            )}
          </div>

          {mediaId !== null && slug !== null && (
            <Button
              variant="outline"
              size="sm"
              disabled={downloadingMediaId !== null}
              onClick={() => download(mediaId, fileName)}
            >
              {isDownloading ? (
                <Spinner className="size-4" />
              ) : (
                <DownloadSimpleIcon className="size-4" />
              )}
              {t('actions.download')}
            </Button>
          )}
        </div>

        {isPreviewOpen && (
          <div className="border-t px-4 py-3">
            <FilePreview
              mediaId={mediaId}
              mediaType={mediaType as string}
              url={url}
              slug={slug}
            />
          </div>
        )}
      </div>
    </NodeViewWrapper>
  );
}
