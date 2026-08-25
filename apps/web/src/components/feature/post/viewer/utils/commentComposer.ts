/**
 * 댓글 입력창은 상세 화면의 사이드 칼럼에, 댓글 버튼은 본문 칼럼에 있어 서로
 * 다른 트리에 놓인다. 상태를 위로 끌어올리는 대신 DOM 식별자 하나로 잇는다.
 */
export const COMMENT_COMPOSER_ID = 'post-comment-composer';

export function focusCommentComposer() {
  const composer = document.getElementById(COMMENT_COMPOSER_ID);

  if (!composer) {
    return;
  }

  composer.scrollIntoView({ behavior: 'smooth', block: 'center' });

  const editable =
    composer.querySelector<HTMLElement>('[contenteditable="true"]') ?? composer;
  editable.focus({ preventScroll: true });
}
