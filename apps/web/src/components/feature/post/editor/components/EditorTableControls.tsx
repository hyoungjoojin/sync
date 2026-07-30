'use client';

import {
  ColumnsPlusLeftIcon,
  ColumnsPlusRightIcon,
  RowsPlusBottomIcon,
  RowsPlusTopIcon,
  TableIcon,
  TrashIcon,
} from '@phosphor-icons/react';
import { NodeSelection } from '@tiptap/pm/state';
import type { EditorState } from '@tiptap/pm/state';
import type { Editor } from '@tiptap/react';
import { useTranslations } from 'next-intl';
import { useCallback, useEffect, useRef, useState } from 'react';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

import { NodeType } from '../extensions/nodes';

interface EditorTableControlsProps {
  editor: Editor;
}

/** 손잡이의 두께와 표에서 떨어진 간격(px). */
const HANDLE_THICKNESS = 10;
const HANDLE_GAP = 4;

type HandleKind = 'row' | 'column';

interface Handle {
  kind: HandleKind;
  index: number;
  /** 이 행/열 전체를 선택하기 위한 양 끝 셀의 위치. */
  anchorCell: number;
  headCell: number;
  left: number;
  top: number;
  width: number;
  height: number;
}

type TableAction =
  | 'add-column-before'
  | 'add-column-after'
  | 'toggle-header-column'
  | 'delete-column'
  | 'add-row-before'
  | 'add-row-after'
  | 'toggle-header-row'
  | 'delete-row'
  | 'delete-table';

const ACTIONS: Record<HandleKind, TableAction[]> = {
  column: [
    'add-column-before',
    'add-column-after',
    'toggle-header-column',
    'delete-column',
  ],
  row: ['add-row-before', 'add-row-after', 'toggle-header-row', 'delete-row'],
};

const ACTION_ICONS: Record<TableAction, React.ReactNode> = {
  'add-column-before': <ColumnsPlusLeftIcon />,
  'add-column-after': <ColumnsPlusRightIcon />,
  'toggle-header-column': <TableIcon />,
  'delete-column': <TrashIcon />,
  'add-row-before': <RowsPlusTopIcon />,
  'add-row-after': <RowsPlusBottomIcon />,
  'toggle-header-row': <TableIcon />,
  'delete-row': <TrashIcon />,
  'delete-table': <TrashIcon />,
};

/** 행/열과 무관하게 두 메뉴 모두에 붙는 표 단위 동작. */
const DELETE_TABLE: TableAction = 'delete-table';

const DESTRUCTIVE_ACTIONS: TableAction[] = [
  'delete-column',
  'delete-row',
  'delete-table',
];

/**
 * 커서가 표 안에 있을 때만 각 열 위와 각 행 왼쪽에 손잡이를 띄운다. 손잡이를
 * 누르면 그 행/열이 선택되고 편집 메뉴가 열린다. 표 DOM을 직접 재서 위치를
 * 잡으므로, 열 너비 조절이나 화면 크기 변화에도 다시 맞춰진다.
 */
export function EditorTableControls({ editor }: EditorTableControlsProps) {
  const t = useTranslations('components.editor.table');
  const overlayRef = useRef<HTMLDivElement>(null);
  const openHandleRef = useRef<string | null>(null);
  const [openHandle, setOpenHandle] = useState<string | null>(null);
  const [handles, setHandles] = useState<Handle[]>([]);

  const sync = useCallback(() => {
    const overlay = overlayRef.current;

    if (!overlay) {
      return;
    }

    // 표가 초점을 잃으면 손잡이도 사라진다. 다만 메뉴가 열려 있거나 손잡이
    // 자체에 초점이 있는 동안은 유지해야 조작이 끊기지 않는다.
    const isActive =
      editor.view.hasFocus() ||
      openHandleRef.current !== null ||
      overlay.contains(document.activeElement);

    setHandles(isActive ? measureHandles(editor, overlay) : []);
  }, [editor]);

  useEffect(() => {
    let frame = 0;

    const update = () => {
      if (frame !== 0) {
        return;
      }

      frame = requestAnimationFrame(() => {
        frame = 0;
        sync();
      });
    };

    editor.on('transaction', update);
    editor.on('focus', update);
    editor.on('blur', update);
    window.addEventListener('resize', update);
    window.addEventListener('scroll', update, true);
    update();

    return () => {
      if (frame !== 0) {
        cancelAnimationFrame(frame);
      }

      editor.off('transaction', update);
      editor.off('focus', update);
      editor.off('blur', update);
      window.removeEventListener('resize', update);
      window.removeEventListener('scroll', update, true);
    };
  }, [editor, sync]);

  const run = (action: TableAction) => {
    const chain = editor.chain().focus();

    switch (action) {
      case 'add-column-before':
        chain.addColumnBefore().run();
        break;
      case 'add-column-after':
        chain.addColumnAfter().run();
        break;
      case 'toggle-header-column':
        chain.toggleHeaderColumn().run();
        break;
      case 'delete-column':
        chain.deleteColumn().run();
        break;
      case 'add-row-before':
        chain.addRowBefore().run();
        break;
      case 'add-row-after':
        chain.addRowAfter().run();
        break;
      case 'toggle-header-row':
        chain.toggleHeaderRow().run();
        break;
      case 'delete-row':
        chain.deleteRow().run();
        break;
      case 'delete-table':
        chain.deleteTable().run();
        break;
    }
  };

  return (
    <div ref={overlayRef} className="pointer-events-none absolute inset-0">
      {handles.map((handle) => {
        const key = `${handle.kind}-${handle.index}`;

        return (
          <DropdownMenu
            key={key}
            modal={false}
            open={openHandle === key}
            onOpenChange={(open) => {
              openHandleRef.current = open ? key : null;
              setOpenHandle(open ? key : null);
            }}
          >
            <DropdownMenuTrigger asChild>
              <button
                type="button"
                // 손잡이를 눌러도 에디터가 초점을 잃지 않아야 선택이 유지된다.
                onMouseDown={(event) => event.preventDefault()}
                onPointerDown={() => {
                  editor.commands.setCellSelection({
                    anchorCell: handle.anchorCell,
                    headCell: handle.headCell,
                  });
                }}
                style={{
                  left: handle.left,
                  top: handle.top,
                  width: handle.width,
                  height: handle.height,
                }}
                className="pointer-events-auto absolute rounded-full bg-border transition-colors hover:bg-primary data-[state=open]:bg-primary"
                aria-label={
                  handle.kind === 'column'
                    ? t('handles.column', { index: handle.index + 1 })
                    : t('handles.row', { index: handle.index + 1 })
                }
              />
            </DropdownMenuTrigger>

            <DropdownMenuContent
              align="start"
              side={handle.kind === 'column' ? 'bottom' : 'right'}
              className="w-auto min-w-48"
            >
              {ACTIONS[handle.kind].map((action) => (
                <DropdownMenuItem
                  key={action}
                  variant={
                    DESTRUCTIVE_ACTIONS.includes(action)
                      ? 'destructive'
                      : 'default'
                  }
                  onSelect={() => run(action)}
                >
                  {ACTION_ICONS[action]}
                  {t(action)}
                </DropdownMenuItem>
              ))}

              <DropdownMenuSeparator />

              <DropdownMenuItem
                variant="destructive"
                onSelect={() => run(DELETE_TABLE)}
              >
                {ACTION_ICONS[DELETE_TABLE]}
                {t(DELETE_TABLE)}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        );
      })}
    </div>
  );
}

function measureHandles(editor: Editor, overlay: HTMLElement): Handle[] {
  const tablePos = findTablePos(editor.state);

  if (tablePos === null) {
    return [];
  }

  const table = resolveTableElement(editor.view.nodeDOM(tablePos));

  if (table === null) {
    return [];
  }

  const rows = Array.from(table.rows);
  const firstRow = rows[0];

  if (firstRow === undefined) {
    return [];
  }

  const origin = overlay.getBoundingClientRect();
  // 표가 가로로 스크롤될 수 있으므로, 보이는 영역을 기준으로 손잡이를 자른다.
  const viewport =
    table.closest('.tableWrapper')?.getBoundingClientRect() ??
    table.getBoundingClientRect();

  const cellPos = (cell: HTMLElement) => editor.view.posAtDOM(cell, 0) - 1;
  const handles: Handle[] = [];

  Array.from(firstRow.cells).forEach((cell, index) => {
    const rect = cell.getBoundingClientRect();
    const center = rect.left + rect.width / 2;

    if (center < viewport.left || center > viewport.right) {
      return;
    }

    const lastCell = rows[rows.length - 1]?.cells[index] ?? cell;

    handles.push({
      kind: 'column',
      index,
      anchorCell: cellPos(cell),
      headCell: cellPos(lastCell),
      left: Math.max(rect.left, viewport.left) - origin.left,
      top: rect.top - origin.top - HANDLE_GAP - HANDLE_THICKNESS,
      width:
        Math.min(rect.right, viewport.right) -
        Math.max(rect.left, viewport.left),
      height: HANDLE_THICKNESS,
    });
  });

  rows.forEach((row, index) => {
    const cells = Array.from(row.cells);
    const first = cells[0];
    const last = cells[cells.length - 1];

    if (first === undefined || last === undefined) {
      return;
    }

    const rect = row.getBoundingClientRect();

    handles.push({
      kind: 'row',
      index,
      anchorCell: cellPos(first),
      headCell: cellPos(last),
      left: viewport.left - origin.left - HANDLE_GAP - HANDLE_THICKNESS,
      top: rect.top - origin.top,
      width: HANDLE_THICKNESS,
      height: rect.height,
    });
  });

  return handles;
}

function findTablePos(state: EditorState): number | null {
  const { selection } = state;

  if (
    selection instanceof NodeSelection &&
    selection.node.type.name === NodeType.Table
  ) {
    return selection.from;
  }

  const { $from } = selection;

  for (let depth = $from.depth; depth > 0; depth -= 1) {
    if ($from.node(depth).type.name === NodeType.Table) {
      return $from.before(depth);
    }
  }

  return null;
}

/** 열 너비 조절이 켜져 있으면 표 노드의 DOM은 `div.tableWrapper`다. */
function resolveTableElement(dom: Node | null): HTMLTableElement | null {
  if (dom instanceof HTMLTableElement) {
    return dom;
  }

  if (dom instanceof HTMLElement) {
    return dom.querySelector('table');
  }

  return null;
}
