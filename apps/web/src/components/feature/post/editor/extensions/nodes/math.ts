import { Mathematics } from '@tiptap/extension-mathematics';
import type { Node as ProseMirrorNode } from '@tiptap/pm/model';
import type { KatexOptions } from 'katex';

import { NodeType } from '.';

export type MathNodeType = NodeType.InlineMath | NodeType.BlockMath;

export interface MathTarget {
  type: MathNodeType;
  /** 편집 대상 노드의 위치. 새로 넣는 경우에는 삽입될 위치다. */
  pos: number;
  latex: string;
  isNew: boolean;
}

/**
 * `throwOnError: false` 로 두면 잘못된 수식도 예외 대신 붉은 원문으로 렌더링된다.
 * 작성 도중에는 늘 불완전한 상태를 거치므로, 그때마다 편집기가 깨지면 안 된다.
 */
const KATEX_OPTIONS: KatexOptions = {
  throwOnError: false,
  strict: 'ignore',
};

/**
 * 수식 노드는 원본 LaTeX 을 감춘 채 렌더링 결과만 보여주는 atom 이라, 클릭으로
 * 편집기를 열어주지 않으면 한 번 넣은 뒤에는 지우는 것 말고 할 수 있는 게 없다.
 */
export function createMathNode(onEdit: (target: MathTarget) => void) {
  const handleClick =
    (type: MathNodeType) => (node: ProseMirrorNode, pos: number) =>
      onEdit({
        type,
        pos,
        latex: typeof node.attrs.latex === 'string' ? node.attrs.latex : '',
        isNew: false,
      });

  return Mathematics.configure({
    katexOptions: KATEX_OPTIONS,
    inlineOptions: { onClick: handleClick(NodeType.InlineMath) },
    blockOptions: { onClick: handleClick(NodeType.BlockMath) },
  });
}

export const ReadOnlyMathNode = Mathematics.configure({
  katexOptions: KATEX_OPTIONS,
});
