'use client';

import {
  CheckIcon,
  CodeIcon,
  LinkSimpleIcon,
  TextBolderIcon,
  TextHOneIcon,
  TextHTwoIcon,
  TextItalicIcon,
  TextStrikethroughIcon,
} from '@phosphor-icons/react';
import { useEditor } from '@tiptap/react';
import { BubbleMenu } from '@tiptap/react/menus';
import { useTranslations } from 'next-intl';
import { useEffect, useRef, useState } from 'react';

import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';

import { NodeType } from '../extensions/nodes';
import { isCellSelection } from '../extensions/nodes/table';

interface EditorBubbleMenuProps {
  editor: ReturnType<typeof useEditor>;
}

export function EditorBubbleMenu({ editor }: EditorBubbleMenuProps) {
  const t = useTranslations('components.editor.link');
  const [isLinkMode, setIsLinkMode] = useState(false);
  const [linkValue, setLinkValue] = useState('');
  const linkInputRef = useRef<HTMLInputElement>(null);

  const openLinkMode = () => {
    setLinkValue((editor?.getAttributes('link').href as string) ?? '');
    setIsLinkMode(true);
  };

  const closeLinkMode = () => {
    setIsLinkMode(false);
    setLinkValue('');
    editor?.chain().focus().run();
  };

  const applyLink = () => {
    if (!editor) return;
    const href = linkValue.trim();
    if (href.length === 0) {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
    } else {
      editor.chain().focus().extendMarkRange('link').setLink({ href }).run();
    }
    closeLinkMode();
  };

  useEffect(() => {
    if (isLinkMode) {
      linkInputRef.current?.focus();
    }
  }, [isLinkMode]);

  const buttons = [
    {
      id: 'bold',
      icon: <TextBolderIcon />,
      isActive: () => editor?.isActive('bold') ?? false,
      onClick: () => editor?.chain().focus().toggleBold().run(),
    },
    {
      id: 'italic',
      icon: <TextItalicIcon />,
      isActive: () => editor?.isActive('italic') ?? false,
      onClick: () => editor?.chain().focus().toggleItalic().run(),
    },
    {
      id: 'strike',
      icon: <TextStrikethroughIcon />,
      isActive: () => editor?.isActive('strike') ?? false,
      onClick: () => editor?.chain().focus().toggleStrike().run(),
    },
    {
      id: 'code',
      icon: <CodeIcon />,
      isActive: () => editor?.isActive('code') ?? false,
      onClick: () => editor?.chain().focus().toggleCode().run(),
    },
    {
      id: 'link',
      icon: <LinkSimpleIcon />,
      isActive: () => editor?.isActive('link') ?? false,
      onClick: openLinkMode,
    },
    null,
    {
      id: 'h1',
      icon: <TextHOneIcon />,
      isActive: () => editor?.isActive('heading', { level: 1 }) ?? false,
      onClick: () => editor?.chain().focus().toggleHeading({ level: 1 }).run(),
    },
    {
      id: 'h2',
      icon: <TextHTwoIcon />,
      isActive: () => editor?.isActive('heading', { level: 2 }) ?? false,
      onClick: () => editor?.chain().focus().toggleHeading({ level: 2 }).run(),
    },
  ];

  return (
    <BubbleMenu
      editor={editor}
      className="flex items-center gap-0.5 rounded-lg border bg-popover p-1 shadow-lg"
      shouldShow={({ editor, view, state }) => {
        if (!view.hasFocus() && !isLinkMode) {
          return false;
        }

        // 서식을 적용할 텍스트가 없는 노드는 서식 메뉴를 띄우지 않는다.
        if (editor.isActive(NodeType.Image) || editor.isActive(NodeType.File)) {
          return false;
        }

        if (isLinkMode) {
          return true;
        }

        // 여러 셀을 고른 상태는 서식이 아니라 표 편집 맥락이므로 표 메뉴에 넘긴다.
        if (isCellSelection(state.selection)) {
          return false;
        }

        const { empty } = state.selection;
        if (empty) {
          return false;
        }

        return true;
      }}
    >
      {isLinkMode ? (
        <div className="flex items-center gap-1">
          <input
            ref={linkInputRef}
            type="url"
            value={linkValue}
            onChange={(e) => setLinkValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                applyLink();
              } else if (e.key === 'Escape') {
                e.preventDefault();
                closeLinkMode();
              }
            }}
            onBlur={() => setIsLinkMode(false)}
            placeholder={t('placeholder')}
            className="h-7 w-56 rounded-md bg-transparent px-2 text-sm outline-none placeholder:text-muted-foreground"
          />
          <button
            type="button"
            onMouseDown={(e) => e.preventDefault()}
            onClick={applyLink}
            className="rounded p-1.5 transition-colors hover:bg-accent"
            aria-label={t('apply')}
          >
            <CheckIcon />
          </button>
        </div>
      ) : (
        buttons.map((btn, i) =>
          btn === null ? (
            <Separator key={i} orientation="vertical" className="mx-1 h-5" />
          ) : (
            <button
              key={btn.id}
              type="button"
              onClick={btn.onClick}
              className={cn(
                'rounded p-1.5 transition-colors hover:bg-accent',
                btn.isActive() && 'bg-accent text-accent-foreground',
              )}
              aria-label={btn.id}
            >
              {btn.icon}
            </button>
          ),
        )
      )}
    </BubbleMenu>
  );
}
