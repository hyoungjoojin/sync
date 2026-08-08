import MarkdownIt from 'markdown-it';

/**
 * 에이전트가 만든 초안의 Markdown 본문을 에디터가 읽을 수 있는 HTML 로 바꾼다.
 *
 * Tiptap 은 HTML 을 초기 콘텐츠로 그대로 받는다. 각 확장(extension)이 가진 `parseHTML` 규칙으로
 * 알아서 자기 스키마에 맞는 노드로 만들기 때문에, 여기서 ProseMirror JSON 을 손으로 조립할 필요가
 * 없다. 스키마를 두 곳에 복제하지 않으니 서로 어긋날 일도 없다 — 실제로 변환을 하는 것은 늘 쓰던
 * 그 에디터다.
 *
 * 변환은 작성자가 초안을 처음 열 때 한 번만 일어난다. 그 다음 저장이 진짜 Tiptap JSON 을
 * `content` 에 넣고 `markdown_content` 를 비우므로, 그 시점부터 이 글은 다른 글과 구별되지 않는다.
 */
const markdown = new MarkdownIt({
  html: false, // 원문의 raw HTML 은 통과시키지 않는다. 본문은 신뢰할 수 없는 입력이다.
  linkify: true,
  breaks: false,
});

export function markdownToHtml(source: string): string {
  return markdown.render(source);
}
