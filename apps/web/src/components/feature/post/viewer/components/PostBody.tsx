'use client';

import type { Editor } from '@tiptap/react';
import { EditorContent } from '@tiptap/react';

import { PostPreviewBody } from './PostPreviewBody';

export interface PostBodyProps {
  editor: Editor | null;
  className?: string;
  /**
   * 유료 게이트(`accessLevel === 'PREVIEW'`)로 본문이 잠긴 경우 대신 보여줄 미리보기 텍스트.
   * 지정되면 에디터 본문 대신 이 텍스트만 렌더링한다.
   */
  lockedPreview?: string;
}

export function PostBody({ editor, className, lockedPreview }: PostBodyProps) {
  if (lockedPreview !== undefined) {
    return <PostPreviewBody preview={lockedPreview} className={className} />;
  }

  return <EditorContent editor={editor} className={className} />;
}
