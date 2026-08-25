'use client';

import {
  CodeBlockIcon,
  CodeIcon,
  PaperPlaneRightIcon,
  TextBolderIcon,
  TextItalicIcon,
} from '@phosphor-icons/react';
import { Placeholder } from '@tiptap/extensions';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { useTranslations } from 'next-intl';
import { forwardRef, useImperativeHandle } from 'react';

import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

import { CodeBlockNode } from '../../editor/extensions/nodes/code';
import { COMMENT_COMPOSER_ID } from '../utils/commentComposer';

export interface CommentComposerHandle {
  clear: () => void;
}

interface CommentComposerProps {
  onSubmit: (content: string) => void;
  isSubmitting: boolean;
}

function ToolbarButton({
  label,
  icon,
  isActive,
  onClick,
}: {
  label: string;
  icon: React.ReactNode;
  isActive: boolean;
  onClick: () => void;
}) {
  return (
    <Button
      type="button"
      variant="ghost"
      size="icon-xs"
      aria-label={label}
      aria-pressed={isActive}
      className={cn(isActive && 'bg-muted text-foreground')}
      onClick={onClick}
    >
      {icon}
    </Button>
  );
}

export const CommentComposer = forwardRef<
  CommentComposerHandle,
  CommentComposerProps
>(function CommentComposer({ onSubmit, isSubmitting }, ref) {
  const t = useTranslations('pages.posts.comments.composer');

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        codeBlock: false,
        heading: false,
        horizontalRule: false,
        link: false,
        underline: false,
      }),
      CodeBlockNode,
      Placeholder.configure({ placeholder: t('placeholder') }),
    ],
    editorProps: {
      attributes: {
        class: 'min-h-10 text-sm focus:outline-none',
      },
    },
    immediatelyRender: false,
  });

  useImperativeHandle(
    ref,
    () => ({
      clear: () => {
        editor?.commands.clearContent(true);
      },
    }),
    [editor],
  );

  const isEmpty = editor?.isEmpty ?? true;

  function handleSubmit() {
    if (!editor || editor.isEmpty) {
      return;
    }

    onSubmit(JSON.stringify(editor.getJSON()));
  }

  return (
    <div
      id={COMMENT_COMPOSER_ID}
      className="focus-within:ring-ring/50 border-input bg-input/30 flex flex-col gap-1.5 rounded-xl border px-3 py-2 transition-colors focus-within:ring-[3px]"
    >
      <div className="text-muted-foreground -ml-1 flex items-center gap-0.5">
        <ToolbarButton
          label={t('toolbar.bold')}
          icon={<TextBolderIcon />}
          isActive={editor?.isActive('bold') ?? false}
          onClick={() => editor?.chain().focus().toggleBold().run()}
        />
        <ToolbarButton
          label={t('toolbar.italic')}
          icon={<TextItalicIcon />}
          isActive={editor?.isActive('italic') ?? false}
          onClick={() => editor?.chain().focus().toggleItalic().run()}
        />
        <ToolbarButton
          label={t('toolbar.code')}
          icon={<CodeIcon />}
          isActive={editor?.isActive('code') ?? false}
          onClick={() => editor?.chain().focus().toggleCode().run()}
        />
        <ToolbarButton
          label={t('toolbar.code-block')}
          icon={<CodeBlockIcon />}
          isActive={editor?.isActive('codeBlock') ?? false}
          onClick={() => editor?.chain().focus().toggleCodeBlock().run()}
        />
      </div>

      <EditorContent editor={editor} />

      <div className="flex justify-end">
        <Button
          size="sm"
          disabled={isEmpty || isSubmitting}
          onClick={handleSubmit}
        >
          <PaperPlaneRightIcon />
          {t('submit')}
        </Button>
      </div>
    </div>
  );
});
