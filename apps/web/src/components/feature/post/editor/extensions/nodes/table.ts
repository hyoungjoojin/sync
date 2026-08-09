import { TableKit } from '@tiptap/extension-table';
import type { Selection } from '@tiptap/pm/state';
import { CellSelection } from '@tiptap/pm/tables';

/**
 * 표. 슬래시 메뉴의 `/table`로 삽입하며, 셀 안에서 Tab/Shift+Tab으로 이동한다.
 * 스타일은 styles/tiptap.scss의 `table` 규칙에서 정의된다.
 */
export const TableNode = TableKit.configure({
  table: {
    resizable: true,
    lastColumnResizable: false,
    allowTableNodeSelection: true,
  },
});

/**
 * 읽기 전용 렌더링용. 열 너비 조절 핸들 없이, 좁은 화면에서 가로 스크롤을
 * 걸 수 있도록 래퍼(`div.tableWrapper`)만 유지한다.
 */
export const ReadOnlyTableNode = TableKit.configure({
  table: {
    resizable: false,
    renderWrapper: true,
  },
});

/** 여러 셀을 한 번에 고른 상태인지. 표 메뉴와 서식 메뉴가 이 값으로 갈린다. */
export function isCellSelection(selection: Selection): boolean {
  return selection instanceof CellSelection;
}
