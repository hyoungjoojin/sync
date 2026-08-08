import { describe, expect, it } from 'vitest';

import { markdownToHtml } from './markdown';

/**
 * 이 테스트가 검증하는 것은 "Tiptap 이 잘 읽어들이는가"가 아니라 "Tiptap 이 읽을 수 있는 형태의
 * HTML 이 나오는가"다. 변환 자체는 브라우저의 에디터가 자기 `parseHTML` 규칙으로 수행하므로,
 * 우리 쪽 책임은 그 규칙들이 기대하는 표준 태그를 내보내는 것까지다.
 */
describe('markdownToHtml', () => {
  it('블록 요소를 표준 HTML 태그로 바꾼다', () => {
    const html = markdownToHtml(
      [
        '# 제목',
        '',
        '본문 **굵게** 그리고 *기울임* 그리고 ~~취소선~~.',
        '',
        '- 항목 1',
        '- 항목 2',
        '',
        '1. 첫째',
        '2. 둘째',
        '',
        '> 인용문',
        '',
        '[링크](https://example.com)',
      ].join('\n'),
    );

    expect(html).toContain('<h1>제목</h1>');
    expect(html).toContain('<strong>굵게</strong>');
    expect(html).toContain('<em>기울임</em>');
    expect(html).toContain('<s>취소선</s>');
    expect(html).toContain('<ul>');
    expect(html).toContain('<ol>');
    expect(html).toContain('<blockquote>');
    expect(html).toContain('<a href="https://example.com">링크</a>');
  });

  it('언어가 지정된 코드 블록은 language- 클래스를 남긴다', () => {
    const html = markdownToHtml('```java\nSystem.out.println("hi");\n```');

    expect(html).toContain('<pre>');
    expect(html).toContain('class="language-java"');
    // 코드 안의 꺾쇠/따옴표는 이스케이프되어야 한다.
    expect(html).toContain('&quot;hi&quot;');
  });

  it('표를 table/tr/td 로 바꾼다', () => {
    const html = markdownToHtml(
      ['| A | B |', '| --- | --- |', '| 1 | 2 |'].join('\n'),
    );

    expect(html).toContain('<table>');
    expect(html).toContain('<th>');
    expect(html).toContain('<td>');
  });

  it('원문에 들어 있는 raw HTML 을 그대로 통과시키지 않는다', () => {
    const html = markdownToHtml('<script>alert(1)</script>\n\n<b>굵게</b>');

    // 본문은 신뢰할 수 없는 입력이다(에이전트가 보낸 값). 태그가 살아서 나가면 안 된다.
    expect(html).not.toContain('<script>');
    expect(html).not.toContain('<b>');
    expect(html).toContain('&lt;script&gt;');
  });

  it('빈 문자열은 빈 결과를 낸다', () => {
    expect(markdownToHtml('').trim()).toBe('');
  });
});
