import { ArrowCounterClockwiseIcon, ImageIcon } from '@phosphor-icons/react';
import { Plugin, PluginKey } from '@tiptap/pm/state';
import type { EditorView } from '@tiptap/pm/view';
import {
  Node,
  NodeViewProps,
  NodeViewWrapper,
  ReactNodeViewRenderer,
  mergeAttributes,
} from '@tiptap/react';
import { useTranslations } from 'next-intl';
import Image from 'next/image';
import { useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import { useUploadMedia } from '@/api/__generated__/media/media';
import { uploadFileToS3 } from '@/api/s3';
import { Button } from '@/components/ui/button';
import { FileInput } from '@/components/ui/input';
import { Spinner } from '@/components/ui/spinner';
import { MAX_FILE_SIZE, cn } from '@/lib/tiptap-utils';

import { NodeType } from '.';

export type ImageNodeAttributes = {
  src: string | null;
  status: 'none' | 'loaded' | 'uploading' | 'uploaded' | 'error';
  mediaId: string | null;
  pendingId: string | null;
};

const pendingFiles = new Map<string, File>();
let pendingFileSequence = 0;

function stashPendingFile(file: File): string {
  pendingFileSequence += 1;
  const pendingId = `pending-${pendingFileSequence}`;
  pendingFiles.set(pendingId, file);
  return pendingId;
}

function takePendingFile(pendingId: string): File | null {
  const file = pendingFiles.get(pendingId) ?? null;
  pendingFiles.delete(pendingId);
  return file;
}

function getImageFiles(transfer: DataTransfer | null): File[] {
  if (!transfer) {
    return [];
  }

  return Array.from(transfer.files).filter((file) =>
    file.type.startsWith('image/'),
  );
}

function hasImageFileTransfer(transfer: DataTransfer | null): boolean {
  if (!transfer) {
    return false;
  }

  return Array.from(transfer.items).some(
    (item) => item.kind === 'file' && item.type.startsWith('image/'),
  );
}

function resolveDropRange(
  view: EditorView,
  event: DragEvent,
): { from: number; to: number } {
  const coordinates = view.posAtCoords({
    left: event.clientX,
    top: event.clientY,
  });

  if (!coordinates) {
    const { from, to } = view.state.selection;
    return { from, to };
  }

  if (coordinates.inside >= 0) {
    const target = view.state.doc.nodeAt(coordinates.inside);

    if (
      target?.type.name === NodeType.Image &&
      target.attrs.status === 'none'
    ) {
      return {
        from: coordinates.inside,
        to: coordinates.inside + target.nodeSize,
      };
    }
  }

  return { from: coordinates.pos, to: coordinates.pos };
}

const imageNodeSchema = {
  name: NodeType.Image,
  group: 'block',
  content: '',
  atom: true,
  selectable: true,
  draggable: true,
  addAttributes() {
    return {
      src: {
        default: null,
      },
      status: {
        default: 'none',
        rendered: false,
      },
      mediaId: {
        default: null,
        parseHTML: (element: Element) => element.getAttribute('data-media-id'),
        renderHTML: (attributes: Record<string, unknown>) =>
          attributes.mediaId ? { 'data-media-id': attributes.mediaId } : {},
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
        tag: 'img[src]',
        getAttrs: (element: Element) => ({
          status: element.getAttribute('src') ? 'uploaded' : 'none',
        }),
      },
    ];
  },
  renderHTML({
    HTMLAttributes,
  }: {
    HTMLAttributes: Record<string, unknown>;
  }): [string, Record<string, unknown>] {
    return ['img', mergeAttributes(HTMLAttributes)];
  },
};

export const ImageNode = Node.create<ImageNodeAttributes>({
  ...imageNodeSchema,
  addNodeView() {
    return ReactNodeViewRenderer(ImageNodeComponent);
  },
  addProseMirrorPlugins() {
    const { editor, name } = this;

    const insertImageFiles = (
      files: File[],
      range: { from: number; to: number },
    ) => {
      editor
        .chain()
        .insertContentAt(
          range,
          files.map((file) => ({
            type: name,
            attrs: {
              status: 'uploading',
              pendingId: stashPendingFile(file),
            },
          })),
        )
        .focus()
        .run();
    };

    return [
      new Plugin({
        key: new PluginKey('imageFileDropPaste'),
        props: {
          handlePaste: (view, event) => {
            const files = getImageFiles(event.clipboardData);
            if (files.length === 0) {
              return false;
            }

            if (event.clipboardData?.types.includes('text/html')) {
              return false;
            }

            event.preventDefault();

            const { from, to } = view.state.selection;
            insertImageFiles(files, { from, to });

            return true;
          },
          handleDrop: (view, event, _slice, moved) => {
            if (moved) {
              return false;
            }

            const files = getImageFiles(event.dataTransfer);
            if (files.length === 0) {
              return false;
            }

            event.preventDefault();

            insertImageFiles(files, resolveDropRange(view, event));

            return true;
          },
        },
      }),
    ];
  },
});

export const ReadOnlyImageNode = Node.create<ImageNodeAttributes>({
  ...imageNodeSchema,
  addNodeView() {
    return ReactNodeViewRenderer(ReadOnlyImageNodeComponent);
  },
});

function ImageNodeComponent({
  node,
  selected,
  updateAttributes,
}: NodeViewProps) {
  const { src, status, pendingId } = node.attrs as ImageNodeAttributes;

  const t = useTranslations('components.editor.image');
  const { mutateAsync: uploadImage } = useUploadMedia();

  const objectURLRef = useRef<string | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);

  useEffect(() => {
    return () => {
      if (objectURLRef.current) {
        URL.revokeObjectURL(objectURLRef.current);
      }
    };
  }, []);

  const handleFileUpload = useCallback(
    (files: File[]) => {
      const file = files[0];
      if (!file) {
        return;
      }

      if (file.size > MAX_FILE_SIZE) {
        toast.error(t('errors.max-size'));
        updateAttributes({ status: 'error' });
        return;
      }

      const objectUrl = URL.createObjectURL(file);
      objectURLRef.current = objectUrl;

      updateAttributes({
        src: objectUrl,
        status: 'uploading',
      });

      uploadImage({
        data: {
          fileName: file.name,
          fileSize: file.size,
          mediaType: file.type,
        },
      })
        .then(({ data: { mediaId, uploadUrl } }) => {
          updateAttributes({
            mediaId,
          });

          return uploadFileToS3({
            uploadUrl,
            file,
          });
        })
        .then(({ success }) => {
          if (!success) {
            throw new Error(t('errors.s3-upload-failed'));
          }

          updateAttributes({
            status: 'uploaded',
          });
          toast.success(t('messages.upload-success'));
        })
        .catch((error) => {
          toast.error(
            error instanceof Error ? error.message : t('errors.upload-failed'),
          );

          updateAttributes({
            status: 'error',
          });
        });
    },
    [t, updateAttributes, uploadImage],
  );

  useEffect(() => {
    if (!pendingId) {
      return;
    }

    const file = takePendingFile(pendingId);
    updateAttributes({ pendingId: null });

    if (!file) {
      return;
    }

    handleFileUpload([file]);
  }, [pendingId, updateAttributes, handleFileUpload]);

  return (
    <NodeViewWrapper className="my-6">
      <div
        className={cn(
          'w-full rounded-lg overflow-hidden',
          selected && 'border-l',
        )}
      >
        {status === 'none' ? (
          <FileInput
            accept="image/*"
            onFileChange={handleFileUpload}
            maxFiles={1}
          >
            <div
              onDragEnter={(event) => {
                if (hasImageFileTransfer(event.dataTransfer)) {
                  setIsDragOver(true);
                }
              }}
              onDragLeave={(event) => {
                if (
                  !event.currentTarget.contains(
                    event.relatedTarget as HTMLElement | null,
                  )
                ) {
                  setIsDragOver(false);
                }
              }}
              onDrop={() => setIsDragOver(false)}
              className={cn(
                'flex w-full flex-col items-center justify-center gap-2 bg-muted py-10 text-muted-foreground transition-colors hover:border-primary/50 hover:bg-muted/50 hover:text-foreground',
                isDragOver && 'ring-2 ring-inset ring-primary/60',
              )}
            >
              <ImageIcon className="size-8" />
              <span className="text-sm font-medium">
                {t('placeholders.upload')}
              </span>
            </div>
          </FileInput>
        ) : (
          <div className="relative aspect-video w-full">
            {src && (
              <Image
                src={src}
                alt={t('alt')}
                fill
                className={cn(
                  'object-cover',
                  status === 'uploading' && 'blur-sm brightness-75',
                  status === 'error' && 'opacity-25',
                )}
                unoptimized
              />
            )}

            {status === 'uploading' && (
              <div className="absolute inset-0 flex items-center justify-center">
                <Spinner className="size-6 border-white border-t-transparent" />
              </div>
            )}

            {status === 'error' && (
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="flex w-full flex-col items-center justify-center gap-3 rounded-lg py-10">
                  <span className="text-xl text-destructive">
                    {t('errors.upload-failed')}
                  </span>

                  <FileInput
                    accept="image/*"
                    onFileChange={handleFileUpload}
                    maxFiles={1}
                  >
                    <Button variant="outline" size="sm">
                      <ArrowCounterClockwiseIcon className="size-4" />
                      {t('actions.retry')}
                    </Button>
                  </FileInput>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </NodeViewWrapper>
  );
}

function ReadOnlyImageNodeComponent({ node }: NodeViewProps) {
  const { src } = node.attrs as ImageNodeAttributes;
  const t = useTranslations('components.editor.image');

  return (
    <NodeViewWrapper className="my-6">
      <div className="w-full overflow-hidden rounded-lg">
        <div className="relative aspect-video w-full">
          {src && (
            <Image
              src={src}
              alt={t('alt')}
              fill
              className="object-cover"
              unoptimized
            />
          )}
        </div>
      </div>
    </NodeViewWrapper>
  );
}
