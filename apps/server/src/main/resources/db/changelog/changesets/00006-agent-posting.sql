--liquibase formatted sql
--changeset skkil:00006-agent-posting

-- 에이전트가 작성한 초안의 본문. 에이전트가 보낸 Markdown 을 서버가 전혀 해석하지 않고
-- 그대로 보관한다. 작성자가 에디터에서 처음 저장하는 순간 Tiptap JSON 이 content 로
-- 들어가고 이 컬럼은 비워진다.
ALTER TABLE posts ADD COLUMN markdown_content TEXT DEFAULT NULL;

-- 어떤 에이전트 클라이언트가 만든 글인지("🤖 {clientName} 으로 작성됨" 배지의 출처).
-- ON DELETE 를 지정하지 않아 기본값 NO ACTION 이 적용되므로, 글이 참조하는 클라이언트
-- 행은 데이터베이스가 삭제를 거부한다. 운영 규칙이 아니라 제약으로 보장된다.
ALTER TABLE posts ADD COLUMN created_via_client_id VARCHAR(100) DEFAULT NULL
    REFERENCES oauth2_registered_client (id);

-- 본문 형식은 별도의 enum 컬럼으로 저장하지 않고 두 컬럼 중 어느 쪽이 채워졌는지로
-- 파생한다(PostScope 가 project_id 에서 파생되는 것과 같은 이유). 따라서 "정확히 한
-- 쪽만 채워져 있다"는 불변식은 애플리케이션이 아니라 제약으로 강제한다.
ALTER TABLE posts ADD CONSTRAINT posts_content_format_check CHECK (
    (content IS NOT NULL AND markdown_content IS NULL)
    OR (content IS NULL AND markdown_content IS NOT NULL)
);
