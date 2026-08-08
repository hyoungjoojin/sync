export enum PostType {
  SHORT = 'SHORT',
  LONG = 'LONG',
  QUESTION = 'QUESTION',
}

export enum PostStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
}

/**
 * 게시글 본문의 형식. 서버가 저장하는 값이 아니라 두 본문 컬럼 중 어느 쪽이 채워져 있는지에서
 * 파생되며, 응답의 `content.format` 으로 내려온다.
 */
export enum PostContentFormat {
  /** 에디터가 저장한 Tiptap JSON. 지금까지의 모든 게시글. */
  TIPTAP_JSON = 'TIPTAP_JSON',
  /** 에이전트가 보낸 Markdown 원문. 작성자가 처음 저장하면 TIPTAP_JSON 이 된다. */
  MARKDOWN = 'MARKDOWN',
}

export enum PostScope {
  PUBLIC = 'PUBLIC',
  WORKSPACE = 'WORKSPACE',
}

export enum PostRecommendationType {
  FOLLOWING = 'FOLLOWING',
  TRENDING = 'TRENDING',
}
