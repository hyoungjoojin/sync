import type { JSONContent } from '@tiptap/react';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';

import type { GetPostResponse } from '@/api/__generated__/types';

import { ReadOnlyCodeBlockNode } from '../../editor/extensions/nodes/code';
import { ReadOnlyEmbedNode } from '../../editor/extensions/nodes/embed';
import { ReadOnlyImageNode } from '../../editor/extensions/nodes/image';
import {
  TaskItemNode,
  TaskListNode,
} from '../../editor/extensions/nodes/tasks';
import { deserialize } from '../../editor/utils/serializer';

const EMPTY_DOC: JSONContent = { type: 'doc', content: [] };

export function useReadOnlyPostEditor(
  content:
    | Pick<NonNullable<GetPostResponse['content']>, 'json' | 'media'>
    | undefined,
) {
  let doc: JSONContent;
  if (content === undefined) {
    // 유료 게이트(PREVIEW)처럼 서버가 본문을 아예 내려주지 않는 경우. 잠긴 본문 대신
    // 무엇을 보여줄지는 호출부(PostCard)가 결정하고, 에디터는 빈 문서를 유지한다.
    doc = EMPTY_DOC;
  } else {
    try {
      doc = deserialize(content.json, content.media);
    } catch (error) {
      console.error('Failed to parse post content', error);
      doc = EMPTY_DOC;
    }
  }

  return useEditor({
    extensions: [
      StarterKit.configure({ codeBlock: false }),
      ReadOnlyCodeBlockNode,
      TaskListNode,
      TaskItemNode,
      ReadOnlyImageNode,
      ReadOnlyEmbedNode,
    ],
    content: doc,
    editable: false,
    immediatelyRender: false,
  });
}
