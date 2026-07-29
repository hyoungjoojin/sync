import {
  AllSelection,
  type EditorState,
  NodeSelection,
  type Selection,
  TextSelection,
  type Transaction,
} from '@tiptap/pm/state';
import { Extension } from '@tiptap/react';

function selectAncestor(state: EditorState, depth: number): Selection {
  const { doc, selection } = state;
  const { $from } = selection;

  for (let level = depth; level > 0; level -= 1) {
    const position = $from.before(level);
    const node = doc.nodeAt(position);

    if (node && NodeSelection.isSelectable(node)) {
      return NodeSelection.create(doc, position);
    }
  }

  return new AllSelection(doc);
}

export function expandSelection(state: EditorState): Selection {
  const { doc, selection } = state;
  const { $from, from, to } = selection;

  if (selection instanceof NodeSelection) {
    return selectAncestor(state, $from.depth);
  }

  const depth = $from.sharedDepth(to);

  if (depth === $from.depth && $from.parent.isTextblock) {
    const start = $from.start(depth);
    const end = $from.end(depth);

    if (from !== start || to !== end) {
      return TextSelection.create(doc, start, end);
    }
  }

  return selectAncestor(state, depth);
}

export const SelectAllExtension = Extension.create({
  name: 'progressiveSelectAll',
  priority: 1000,
  addKeyboardShortcuts() {
    return {
      'Mod-a': ({ editor }) => {
        const { state, view } = editor;
        const selection = expandSelection(state);

        if (!selection.eq(state.selection)) {
          const transaction: Transaction = state.tr.setSelection(selection);
          view.dispatch(transaction);
        }

        return true;
      },
    };
  },
});
