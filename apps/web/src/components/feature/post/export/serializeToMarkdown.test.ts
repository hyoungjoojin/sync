import { describe, expect, it } from 'vitest';

import { serializeToMarkdown } from './serializeToMarkdown';

const options = { resolveAsset: () => null };

describe('serializeToMarkdown - 수식', () => {
  it('블록 수식을 $$ 구분자로 감싼다', () => {
    const markdown = serializeToMarkdown(
      {
        type: 'doc',
        content: [{ type: 'blockMath', attrs: { latex: 'E = mc^2' } }],
      },
      options,
    );

    expect(markdown).toBe('$$\nE = mc^2\n$$\n');
  });

  it('인라인 수식을 앞뒤 텍스트와 이어 붙인다', () => {
    const markdown = serializeToMarkdown(
      {
        type: 'doc',
        content: [
          {
            type: 'paragraph',
            content: [
              { type: 'text', text: '반지름이 ' },
              { type: 'inlineMath', attrs: { latex: 'r' } },
              { type: 'text', text: ' 인 원' },
            ],
          },
        ],
      },
      options,
    );

    expect(markdown).toBe('반지름이 $r$ 인 원\n');
  });

  it('LaTeX 이 비어 있는 수식은 생략한다', () => {
    const markdown = serializeToMarkdown(
      {
        type: 'doc',
        content: [{ type: 'blockMath', attrs: { latex: '   ' } }],
      },
      options,
    );

    expect(markdown).toBe('');
  });
});
