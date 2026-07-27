import { describe, expect, it } from 'vitest';

import {
  DEFAULT_OG_IMAGE,
  type PostSeoSource,
  buildPostJsonLd,
  createPostMetadata,
  createTagMetadata,
  getPostCanonicalUrl,
  isPostIndexable,
  toAbsoluteUrl,
} from './seo';

const FALLBACK_DESCRIPTION = '기본 설명';

function createPost(overrides: Partial<PostSeoSource> = {}): PostSeoSource {
  return {
    slug: 'spring-security-guide',
    title: 'Spring Security 접근 제어',
    preview: '공개 게시물의 접근 제어 정책을 설명합니다.',
    status: 'PUBLISHED',
    scope: 'PUBLIC',
    author: {
      name: '김개발',
      handle: 'developer',
    },
    tags: [{ name: 'spring' }, { name: 'security' }],
    previewMedia: [{ url: 'https://cdn.example.com/cover.png' }],
    createdAt: '2026-07-24T01:00:00Z',
    updatedAt: '2026-07-24T02:00:00Z',
    ...overrides,
  };
}

describe('게시물 SEO 정책', () => {
  it('개인 공개 게시물을 색인하고 개인 게시물 URL을 사용한다', () => {
    const post = createPost();
    const metadata = createPostMetadata(post, FALLBACK_DESCRIPTION);

    expect(isPostIndexable(post)).toBe(true);
    expect(getPostCanonicalUrl(post).toString()).toBe(
      'https://sync.example.com/posts/spring-security-guide',
    );
    expect(metadata.robots).toMatchObject({
      index: true,
      follow: true,
    });
    expect(metadata.alternates?.canonical).toEqual(
      new URL('https://sync.example.com/posts/spring-security-guide'),
    );
  });

  it('공개 프로젝트 게시물을 색인하고 프로젝트 URL을 사용한다', () => {
    const post = createPost({
      scope: 'WORKSPACE',
      project: {
        handle: 'sync',
        name: 'SYNC',
        isPublic: true,
      },
    });

    expect(isPostIndexable(post)).toBe(true);
    expect(getPostCanonicalUrl(post).toString()).toBe(
      'https://sync.example.com/projects/sync/posts/spring-security-guide',
    );
  });

  it('비공개 프로젝트 게시물을 색인하지 않는다', () => {
    const post = createPost({
      scope: 'WORKSPACE',
      project: {
        handle: 'private-team',
        name: '비공개 팀',
        isPublic: false,
      },
    });
    const metadata = createPostMetadata(post, FALLBACK_DESCRIPTION);

    expect(isPostIndexable(post)).toBe(false);
    expect(metadata.robots).toMatchObject({
      index: false,
      follow: false,
      noarchive: true,
    });
  });

  it('초안은 소속 공간과 관계없이 색인하지 않는다', () => {
    const personalDraft = createPost({ status: 'DRAFT' });
    const projectDraft = createPost({
      status: 'DRAFT',
      scope: 'WORKSPACE',
      project: {
        handle: 'sync',
        name: 'SYNC',
        isPublic: true,
      },
    });

    expect(isPostIndexable(personalDraft)).toBe(false);
    expect(isPostIndexable(projectDraft)).toBe(false);
  });

  it('제목이 없으면 미리보기로 제목을 만들고 메타데이터를 완성한다', () => {
    const post = createPost({
      title: null,
      preview: '가'.repeat(50),
    });
    const metadata = createPostMetadata(post, FALLBACK_DESCRIPTION);

    expect(metadata.title).toBe(`${'가'.repeat(39)}…`);
    expect(metadata.description).toBe('가'.repeat(50));
    expect(metadata.openGraph).toMatchObject({
      type: 'article',
      locale: 'ko_KR',
      siteName: 'sync',
      publishedTime: post.createdAt,
      modifiedTime: post.updatedAt,
      tags: ['spring', 'security'],
    });
    expect(metadata.twitter).toMatchObject({
      card: 'summary_large_image',
      images: ['https://sync.example.com/og/posts/spring-security-guide'],
    });
  });

  it('만료되는 서명 URL 대신 불변 OG 라우트를 og:image로 사용한다', () => {
    const metadata = createPostMetadata(createPost(), FALLBACK_DESCRIPTION);

    expect(metadata.openGraph?.images).toEqual([
      {
        url: 'https://sync.example.com/og/posts/spring-security-guide',
        alt: 'Spring Security 접근 제어',
      },
    ]);
  });
});

describe('절대 URL 변환', () => {
  it('상대 경로는 사이트 오리진 기준으로 변환한다', () => {
    expect(toAbsoluteUrl(DEFAULT_OG_IMAGE)).toBe(
      'https://sync.example.com/og-default.png',
    );
  });

  it('이미 절대 URL이면 그대로 둔다', () => {
    expect(toAbsoluteUrl('https://cdn.example.com/cover.png')).toBe(
      'https://cdn.example.com/cover.png',
    );
  });
});

describe('태그 SEO 정책', () => {
  const FALLBACK_TAG_DESCRIPTION = 'spring 태그 글 모음';

  it('전역 태그는 색인하고 전역 태그 URL을 정규 URL로 쓴다', () => {
    const metadata = createTagMetadata(
      { id: 7, name: 'spring', description: 'Spring 프레임워크 관련 글' },
      FALLBACK_TAG_DESCRIPTION,
    );

    expect(metadata.title).toBe('spring');
    expect(metadata.description).toBe('Spring 프레임워크 관련 글');
    expect(metadata.robots).toMatchObject({ index: true, follow: true });
    expect(metadata.alternates?.canonical).toEqual(
      new URL('https://sync.example.com/tags/7'),
    );
  });

  it('설명이 비어 있으면 폴백 설명을 쓴다', () => {
    const metadata = createTagMetadata(
      { id: 7, name: 'spring', description: '   ' },
      FALLBACK_TAG_DESCRIPTION,
    );

    expect(metadata.description).toBe(FALLBACK_TAG_DESCRIPTION);
  });

  it('프로젝트 태그는 색인하지 않고 프로젝트 태그 URL을 쓴다', () => {
    const metadata = createTagMetadata(
      { id: 7, name: 'spring', description: '팀 태그', projectHandle: 'sync' },
      FALLBACK_TAG_DESCRIPTION,
    );

    expect(metadata.robots).toMatchObject({
      index: false,
      follow: false,
      noarchive: true,
    });
    expect(metadata.alternates?.canonical).toEqual(
      new URL('https://sync.example.com/projects/sync/tags/7'),
    );
  });
});

describe('게시물 JSON-LD', () => {
  it('개인 게시물의 구조화 데이터를 만든다', () => {
    const post = createPost();
    const jsonLd = buildPostJsonLd(post, '/posts/spring-security-guide');

    expect(jsonLd).toMatchObject({
      '@context': 'https://schema.org',
      '@type': 'BlogPosting',
      url: 'https://sync.example.com/posts/spring-security-guide',
      headline: 'Spring Security 접근 제어',
      datePublished: post.createdAt,
      dateModified: post.updatedAt,
      inLanguage: 'ko-KR',
      keywords: ['spring', 'security'],
      author: {
        '@type': 'Person',
        name: '김개발',
        url: 'https://sync.example.com/@developer',
      },
    });
    expect(jsonLd.image).toEqual([
      'https://sync.example.com/og/posts/spring-security-guide',
    ]);
  });

  it('프로젝트 게시물에는 소속 프로젝트를 표기한다', () => {
    const post = createPost({
      scope: 'WORKSPACE',
      project: { handle: 'sync', name: 'SYNC', isPublic: true },
    });
    const jsonLd = buildPostJsonLd(
      post,
      '/projects/sync/posts/spring-security-guide',
    );

    expect(jsonLd).toMatchObject({
      url: 'https://sync.example.com/projects/sync/posts/spring-security-guide',
      isPartOf: { '@type': 'Blog', name: 'SYNC' },
    });
  });
});
