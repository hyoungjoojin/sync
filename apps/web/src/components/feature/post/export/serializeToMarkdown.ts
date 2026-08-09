import type { JSONContent } from '@tiptap/react';

import { NodeType } from '../editor/extensions/nodes';

export interface MarkdownAsset {
  /** 마크다운 파일 기준 상대 경로 */
  path: string;
  /** 링크 이름으로 쓸 원본 파일 이름 */
  fileName: string | null;
}

interface MarkdownSerializerOptions {
  /**
   * mediaId를 마크다운이 참조할 자산으로 변환한다. `null`을 반환하면 해당 이미지나
   * 첨부 파일은 결과물에서 생략된다.
   */
  resolveAsset: (mediaId: string) => MarkdownAsset | null;
}

type ListKind = 'bullet' | 'ordered' | 'task';

const MAX_HEADING_LEVEL = 6;

const EMPHASIS_MARKS: Array<{ type: string; open: string; close: string }> = [
  { type: 'strike', open: '~~', close: '~~' },
  { type: 'italic', open: '_', close: '_' },
  { type: 'bold', open: '**', close: '**' },
  { type: 'underline', open: '<u>', close: '</u>' },
];

/**
 * Tiptap 문서(JSON)를 마크다운으로 직렬화한다. 에디터에 등록된 노드
 * (`useReadOnlyPostEditor`)만 다루고, 알 수 없는 노드는 자식으로 내려가
 * 텍스트를 잃지 않도록 한다.
 */
export function serializeToMarkdown(
  doc: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  const body = serializeBlocks(doc.content ?? [], options);

  return body.length > 0 ? `${body}\n` : '';
}

function serializeBlocks(
  nodes: JSONContent[],
  options: MarkdownSerializerOptions,
): string {
  return nodes
    .map((node) => serializeBlock(node, options))
    .filter((block) => block.length > 0)
    .join('\n\n');
}

function serializeBlock(
  node: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  switch (node.type) {
    case 'paragraph':
      return escapeBlockStart(serializeInline(node.content ?? [], options));

    case 'heading': {
      const text = serializeInline(node.content ?? [], options);

      if (text.length === 0) {
        return '';
      }

      return `${'#'.repeat(headingLevel(node.attrs?.level))} ${text}`;
    }

    case 'blockquote':
      return prefixLines(serializeBlocks(node.content ?? [], options), '>');

    case 'bulletList':
      return serializeList(node, options, 'bullet');

    case 'orderedList':
      return serializeList(node, options, 'ordered');

    case 'taskList':
      return serializeList(node, options, 'task');

    case 'codeBlock':
      return serializeCodeBlock(node);

    case 'horizontalRule':
      return '---';

    case NodeType.Table:
      return serializeTable(node, options);

    case NodeType.Image:
      return serializeImage(node, options);

    case NodeType.File:
      return serializeFile(node, options);

    case NodeType.Embed:
      return serializeEmbed(node);

    default: {
      if (typeof node.text === 'string' || node.type === 'hardBreak') {
        return escapeBlockStart(serializeInline([node], options));
      }

      return serializeBlocks(node.content ?? [], options);
    }
  }
}

function serializeList(
  node: JSONContent,
  options: MarkdownSerializerOptions,
  kind: ListKind,
): string {
  const items = node.content ?? [];
  const start = kind === 'ordered' ? positiveInt(node.attrs?.start, 1) : 1;

  const rendered = items.map((item, index) => {
    const marker = listMarker(kind, start + index, item);
    const body = serializeBlocks(item.content ?? [], options);

    return indentWithMarker(body, marker);
  });

  if (rendered.length === 0) {
    return '';
  }

  const isLoose = rendered.some((item) => item.includes('\n\n'));

  return rendered.join(isLoose ? '\n\n' : '\n');
}

function listMarker(
  kind: ListKind,
  ordinal: number,
  item: JSONContent,
): string {
  switch (kind) {
    case 'ordered':
      return `${ordinal}. `;

    case 'task':
      return item.attrs?.checked === true ? '- [x] ' : '- [ ] ';

    default:
      return '- ';
  }
}

function indentWithMarker(body: string, marker: string): string {
  const indent = ' '.repeat(marker.length);
  const lines = body.split('\n');

  return lines
    .map((line, index) => {
      if (index === 0) {
        return `${marker}${line}`.trimEnd();
      }

      return line.length > 0 ? `${indent}${line}` : '';
    })
    .join('\n');
}

function prefixLines(body: string, prefix: string): string {
  if (body.length === 0) {
    return '';
  }

  return body
    .split('\n')
    .map((line) => (line.length > 0 ? `${prefix} ${line}` : prefix))
    .join('\n');
}

function serializeCodeBlock(node: JSONContent): string {
  const language =
    typeof node.attrs?.language === 'string' ? node.attrs.language : '';
  const code = collectPlainText(node.content ?? []);
  const fence = '`'.repeat(Math.max(3, longestRun(code, '`') + 1));

  return `${fence}${language}\n${code}\n${fence}`;
}

function serializeImage(
  node: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  const asset = resolveNodeAsset(node, options);

  return asset === null ? '' : `![](${encodeLinkTarget(asset.path)})`;
}

/**
 * 마크다운에는 첨부 파일이라는 개념이 없으므로 평범한 링크로 낮춘다.
 */
function serializeFile(
  node: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  const asset = resolveNodeAsset(node, options);

  if (asset === null) {
    return '';
  }

  const label =
    asset.fileName?.trim() || asset.path.split('/').pop() || asset.path;

  return `[${escapeInline(label)}](${encodeLinkTarget(asset.path)})`;
}

function resolveNodeAsset(
  node: JSONContent,
  options: MarkdownSerializerOptions,
): MarkdownAsset | null {
  const mediaId = node.attrs?.mediaId;

  if (typeof mediaId !== 'string' && typeof mediaId !== 'number') {
    return null;
  }

  return options.resolveAsset(String(mediaId));
}

/**
 * GFM 표로 옮긴다. GFM에는 헤더 없는 표가 없어 첫 행이 헤더 셀이 아니면 빈 헤더를
 * 만들고, 병합 셀(colspan/rowspan)은 표현할 수 없어 무시한다.
 */
function serializeTable(
  node: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  const rows = (node.content ?? []).filter((row) => row.type === 'tableRow');

  if (rows.length === 0) {
    return '';
  }

  const cells = rows.map((row) =>
    (row.content ?? []).map((cell) => serializeTableCell(cell, options)),
  );
  const width = Math.max(...cells.map((row) => row.length));

  if (width === 0) {
    return '';
  }

  const firstRow = rows[0]?.content ?? [];
  const hasHeaderRow =
    firstRow.length > 0 &&
    firstRow.every((cell) => cell.type === 'tableHeader');

  const header = hasHeaderRow
    ? (cells[0] as string[])
    : Array.from({ length: width }, () => '');
  const body = hasHeaderRow ? cells.slice(1) : cells;

  return [
    tableRow(header, width),
    tableRow(
      Array.from({ length: width }, () => '---'),
      width,
    ),
    ...body.map((row) => tableRow(row, width)),
  ].join('\n');
}

function tableRow(cells: string[], width: number): string {
  const padded = Array.from({ length: width }, (_unused, index) =>
    index < cells.length ? (cells[index] as string) : '',
  );

  return `| ${padded.join(' | ')} |`;
}

function serializeTableCell(
  cell: JSONContent,
  options: MarkdownSerializerOptions,
): string {
  // 셀은 한 줄이어야 하므로 블록 구분을 <br> 로 낮춘다. 줄바꿈 앞의 `\` 는
  // 마크다운의 강제 개행 표시이므로 <br> 로 바뀔 때 함께 걷어낸다.
  return serializeBlocks(cell.content ?? [], options)
    .replace(/\|/g, '\\|')
    .replace(/\\?\n+/g, '<br>')
    .trim();
}

function serializeEmbed(node: JSONContent): string {
  const url = node.attrs?.url;

  if (typeof url !== 'string' || url.length === 0) {
    return '';
  }

  return `[${escapeInline(url)}](${encodeLinkTarget(url)})`;
}

/**
 * 인라인 노드를 직렬화한다. Tiptap은 같은 마크를 가진 텍스트를 여러 노드로
 * 쪼개는 일이 잦으므로, 동일한 마크가 이어지는 구간을 합쳐 `**a****b**` 같은
 * 결과를 피한다.
 */
function serializeInline(
  nodes: JSONContent[],
  options: MarkdownSerializerOptions,
): string {
  const parts: string[] = [];
  let run: { signature: string; marks: JSONContent[]; text: string } | null =
    null;

  const flush = () => {
    if (run !== null) {
      parts.push(applyMarks(run.text, run.marks));
      run = null;
    }
  };

  for (const node of nodes) {
    if (node.type === 'hardBreak') {
      flush();
      parts.push('\\\n');
      continue;
    }

    if (node.type === NodeType.Image) {
      flush();
      parts.push(serializeImage(node, options));
      continue;
    }

    if (node.type === NodeType.File) {
      flush();
      parts.push(serializeFile(node, options));
      continue;
    }

    if (typeof node.text !== 'string') {
      flush();
      parts.push(serializeInline(node.content ?? [], options));
      continue;
    }

    const marks = node.marks ?? [];
    const signature = markSignature(marks);

    if (run !== null && run.signature === signature) {
      run.text += node.text;
      continue;
    }

    flush();
    run = { signature, marks, text: node.text };
  }

  flush();

  return parts.join('');
}

function markSignature(marks: JSONContent[]): string {
  return marks
    .map((mark) => `${mark.type}:${JSON.stringify(mark.attrs ?? {})}`)
    .sort()
    .join('|');
}

function applyMarks(text: string, marks: JSONContent[]): string {
  const hasMark = (type: string) => marks.some((mark) => mark.type === type);

  let result = hasMark('code') ? wrapInlineCode(text) : escapeInline(text);

  for (const { type, open, close } of EMPHASIS_MARKS) {
    if (hasMark(type)) {
      result = wrapKeepingOuterSpaces(result, open, close);
    }
  }

  const link = marks.find((mark) => mark.type === 'link');
  const href = link?.attrs?.href;

  if (typeof href === 'string' && href.length > 0) {
    result = `[${result}](${encodeLinkTarget(href)})`;
  }

  return result;
}

/**
 * `** 강조 **` 는 렌더링되지 않으므로 앞뒤 공백을 구분자 밖으로 밀어낸다.
 */
function wrapKeepingOuterSpaces(
  text: string,
  open: string,
  close: string,
): string {
  const match = text.match(/^(\s*)([\s\S]*?)(\s*)$/);

  if (match === null) {
    return text;
  }

  const leading = match[1] ?? '';
  const core = match[2] ?? '';
  const trailing = match[3] ?? '';

  if (core.length === 0) {
    return text;
  }

  return `${leading}${open}${core}${close}${trailing}`;
}

function wrapInlineCode(text: string): string {
  const fence = '`'.repeat(longestRun(text, '`') + 1);
  const pad = text.startsWith('`') || text.endsWith('`') ? ' ' : '';

  return `${fence}${pad}${text}${pad}${fence}`;
}

function escapeInline(text: string): string {
  const escaped = text.replace(/([\\`*[\]<])/g, '\\$1');

  // `_` 는 CommonMark에서 단어 내부일 때 강조로 해석되지 않으므로,
  // snake_case 를 `snake\_case` 로 만들지 않도록 경계에서만 이스케이프한다.
  return escaped.replace(/_/g, (match, offset: number) => {
    const before = escaped[offset - 1];
    const after = escaped[offset + 1];

    return isWordChar(before) && isWordChar(after) ? match : '\\_';
  });
}

/**
 * 문단 첫 글자가 제목/목록/인용 같은 블록 문법으로 읽히는 것을 막는다.
 */
function escapeBlockStart(text: string): string {
  const numbered = text.match(/^(\s*)(\d+)([.)])(?=\s)/);

  if (numbered !== null) {
    const [matched, leading, digits, delimiter] = numbered;

    return `${leading}${digits}\\${delimiter}${text.slice(matched.length)}`;
  }

  return text.replace(
    /^(\s*)([#>+\-=~|])/,
    (_match, leading: string, token: string) => `${leading}\\${token}`,
  );
}

function encodeLinkTarget(target: string): string {
  return /[\s()<>]/.test(target)
    ? `<${target.replace(/([<>])/g, '\\$1')}>`
    : target;
}

function collectPlainText(nodes: JSONContent[]): string {
  return nodes
    .map((node) => {
      if (typeof node.text === 'string') {
        return node.text;
      }

      if (node.type === 'hardBreak') {
        return '\n';
      }

      return collectPlainText(node.content ?? []);
    })
    .join('');
}

function longestRun(text: string, character: string): number {
  let longest = 0;
  let current = 0;

  for (const char of text) {
    current = char === character ? current + 1 : 0;
    longest = Math.max(longest, current);
  }

  return longest;
}

function isWordChar(char: string | undefined): boolean {
  return char !== undefined && /[\p{L}\p{N}]/u.test(char);
}

function headingLevel(level: unknown): number {
  return Math.min(MAX_HEADING_LEVEL, positiveInt(level, 1));
}

function positiveInt(value: unknown, fallback: number): number {
  const parsed = Number(value);

  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}
