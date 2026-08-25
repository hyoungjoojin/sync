import type { JSONContent } from '@tiptap/core';
import { generateHTML } from '@tiptap/html';
import StarterKit from '@tiptap/starter-kit';

import { baseCodeBlock } from '../../editor/extensions/nodes/code.schema';

const COMMENT_EXTENSIONS = [
  StarterKit.configure({
    codeBlock: false,
    heading: false,
    horizontalRule: false,
    link: false,
    underline: false,
  }),
  baseCodeBlock,
];

export function renderCommentHtml(content: string): string | null {
  let doc: JSONContent;
  try {
    doc = JSON.parse(content) as JSONContent;
  } catch {
    return null;
  }

  if (doc?.type !== 'doc' || !Array.isArray(doc.content)) {
    return null;
  }

  try {
    return generateHTML(doc, COMMENT_EXTENSIONS);
  } catch {
    return null;
  }
}
