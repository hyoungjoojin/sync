import { Plugin, PluginKey } from '@tiptap/pm/state';
import type { EditorView } from '@tiptap/pm/view';
import { Extension } from '@tiptap/react';

import {
  isAttachmentFile,
  isAttachmentMediaType,
  isImageFile,
} from '@/lib/tiptap-utils';

import { NodeType } from './nodes';

const pendingFiles = new Map<string, File>();
let pendingFileSequence = 0;

export function stashPendingFile(file: File): string {
  pendingFileSequence += 1;
  const pendingId = `pending-${pendingFileSequence}`;
  pendingFiles.set(pendingId, file);

  return pendingId;
}

export function takePendingFile(pendingId: string): File | null {
  const file = pendingFiles.get(pendingId) ?? null;
  pendingFiles.delete(pendingId);

  return file;
}

/**
 * 방금 올린 파일을 mediaId 로 붙잡아 둔다. 저장 전에는 내려받을 URL이 없으므로
 * 작성 중 미리보기는 이 원본을 그대로 읽는다. objectURL 을 만들어 두는 방식은
 * 회수 시점을 노드 뷰 수명에 맞춰야 해서 깨지기 쉽다.
 */
const localFiles = new Map<string, File>();

export function rememberLocalFile(mediaId: string, file: File): void {
  localFiles.set(mediaId, file);
}

export function getLocalFile(mediaId: string | null): File | null {
  return mediaId ? (localFiles.get(mediaId) ?? null) : null;
}

const MEDIA_NODE_TYPES: string[] = [NodeType.Image, NodeType.File];

function mediaNodeTypeFor(file: File): NodeType | null {
  if (isImageFile(file)) {
    return NodeType.Image;
  }

  if (isAttachmentFile(file)) {
    return NodeType.File;
  }

  return null;
}

/**
 * 드롭·붙여넣기된 파일 하나하나를 타입에 맞는 노드로 나눠 담는다. 이미지 노드와 파일
 * 노드가 각자 플러그인을 두면 이미지와 문서를 함께 떨어뜨렸을 때 한쪽만 먼저 이벤트를
 * 소비해 다른 쪽이 조용히 사라지므로, 분배는 이 한 곳에서만 한다.
 */
function collectSupportedFiles(
  transfer: DataTransfer | null,
): Array<{ file: File; type: NodeType }> {
  if (!transfer) {
    return [];
  }

  return Array.from(transfer.files).flatMap((file) => {
    const type = mediaNodeTypeFor(file);

    return type ? [{ file, type }] : [];
  });
}

export function hasSupportedFileTransfer(
  transfer: DataTransfer | null,
): boolean {
  if (!transfer) {
    return false;
  }

  return Array.from(transfer.items).some(
    (item) =>
      item.kind === 'file' &&
      (item.type.startsWith('image/') || isAttachmentMediaType(item.type)),
  );
}

/**
 * 아직 파일이 선택되지 않은 빈 미디어 노드 위에 떨어뜨렸다면 그 노드를 대체하고,
 * 그렇지 않으면 커서 위치에 새로 끼워 넣는다.
 */
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
      target &&
      MEDIA_NODE_TYPES.includes(target.type.name) &&
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

export const MediaDropPasteExtension = Extension.create({
  name: 'mediaDropPaste',

  addProseMirrorPlugins() {
    const { editor } = this;

    const insertMediaFiles = (
      files: Array<{ file: File; type: NodeType }>,
      range: { from: number; to: number },
    ) => {
      editor
        .chain()
        .insertContentAt(
          range,
          files.map(({ file, type }) => ({
            type,
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
        key: new PluginKey('mediaFileDropPaste'),
        props: {
          handlePaste: (view, event) => {
            const files = collectSupportedFiles(event.clipboardData);
            if (files.length === 0) {
              return false;
            }

            if (event.clipboardData?.types.includes('text/html')) {
              return false;
            }

            event.preventDefault();

            const { from, to } = view.state.selection;
            insertMediaFiles(files, { from, to });

            return true;
          },
          handleDrop: (view, event, _slice, moved) => {
            if (moved) {
              return false;
            }

            const files = collectSupportedFiles(event.dataTransfer);
            if (files.length === 0) {
              return false;
            }

            event.preventDefault();

            insertMediaFiles(files, resolveDropRange(view, event));

            return true;
          },
        },
      }),
    ];
  },
});
