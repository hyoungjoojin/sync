import { useMemo } from 'react';

import { renderCommentHtml } from '../utils/renderCommentHtml';

interface CommentBodyProps {
  content?: string | null;
}

export function CommentBody({ content }: CommentBodyProps) {
  const html = useMemo(
    () => (content ? renderCommentHtml(content) : null),
    [content],
  );

  if (html !== null) {
    return (
      <div
        className="tiptap text-sm break-words [&_pre]:my-2 [&_pre]:overflow-x-auto [&_pre]:rounded-lg [&_pre]:border [&_pre]:bg-muted/40 [&_pre]:p-3"
        dangerouslySetInnerHTML={{ __html: html }}
      />
    );
  }

  return <p className="text-sm break-words whitespace-pre-wrap">{content}</p>;
}
