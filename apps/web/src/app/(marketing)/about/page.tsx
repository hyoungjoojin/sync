import Link from 'next/link';

import { LinkButton } from '@/components/ui/button';
import { Copyright } from '@/components/ui/copyright';
import { Logo } from '@/components/ui/logo';
import { cn } from '@/lib/utils';
import ROUTES from '@/util/routes';

// 랜딩 페이지 — 하드코딩된 한국어 문자열 사용 (i18n 미적용)

const MONO = 'font-mono tracking-tight';

function Eyebrow({ children }: { children: React.ReactNode }) {
  return (
    <span
      className={cn(
        MONO,
        'inline-flex items-center gap-2 text-xs uppercase text-muted-foreground',
      )}
    >
      <span className="size-1.5 rounded-full bg-primary" />
      {children}
    </span>
  );
}

function TypeTag({ label }: { label: string }) {
  return (
    <span
      className={cn(
        MONO,
        'rounded-sm border border-border px-1.5 py-0.5 text-[10px] uppercase text-muted-foreground',
      )}
    >
      {label}
    </span>
  );
}

/* 시그니처 요소: "한 번 답하면 영원히 신뢰할 수 있는" 정본 답변 카드 */
function CanonicalCard() {
  return (
    <div className="rounded-xl border border-border bg-card p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <span
          className={cn(MONO, 'text-[11px] uppercase text-muted-foreground')}
        >
          question · #배포
        </span>
        <span className="flex items-center gap-1.5 rounded-full bg-primary/10 px-2 py-0.5 text-[11px] font-medium text-primary">
          <svg viewBox="0 0 24 24" className="size-3" fill="none">
            <path
              d="M20 6 9 17l-5-5"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          정본 답변
        </span>
      </div>

      <p className="mt-3 text-[15px] font-medium leading-snug">
        스테이징 서버는 어떻게 재배포하나요?
      </p>
      <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
        <code className="rounded bg-muted px-1 py-0.5 text-[13px]">
          make deploy staging
        </code>{' '}
        한 줄이면 됩니다. 파이프라인이 이미지 빌드부터 헬스체크까지 처리해요.
      </p>

      <div className="mt-4 flex items-center justify-between border-t border-border pt-3">
        <span className={cn(MONO, 'text-[11px] text-muted-foreground')}>
          last_verified_at · 3일 전
        </span>
        <span className="flex items-center gap-1.5 text-[11px] font-medium text-primary">
          <span className="size-1.5 animate-pulse rounded-full bg-primary" />
          최신 상태 유지 중
        </span>
      </div>
    </div>
  );
}

function BentoTile({
  className,
  eyebrow,
  title,
  children,
}: {
  className?: string;
  eyebrow: string;
  title: string;
  children?: React.ReactNode;
}) {
  return (
    <div
      className={cn(
        'flex flex-col rounded-xl border border-border bg-card p-6',
        className,
      )}
    >
      <span className={cn(MONO, 'text-[11px] uppercase text-muted-foreground')}>
        {eyebrow}
      </span>
      <h3 className="mt-2 text-lg font-medium leading-snug">{title}</h3>
      {children}
    </div>
  );
}

export default function About() {
  return (
    <div className="flex min-h-screen w-full flex-col bg-background text-foreground">
      {/* Nav */}
      <header className="sticky top-0 z-20 border-b border-border/70 bg-background/80 backdrop-blur">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-4">
          <Logo />
          <div className="flex items-center gap-2">
            <LinkButton variant="ghost" href={ROUTES.LOGIN()}>
              로그인
            </LinkButton>
            <LinkButton href={ROUTES.REGISTER()}>시작하기</LinkButton>
          </div>
        </div>
      </header>

      <main className="flex-1">
        {/* Hero */}
        <section className="relative overflow-hidden">
          <div
            className="pointer-events-none absolute inset-0 -z-10 opacity-[0.04]"
            style={{
              backgroundImage:
                'radial-gradient(currentColor 1px, transparent 1px)',
              backgroundSize: '32px 32px',
            }}
          />
          <div className="mx-auto grid w-full max-w-6xl items-center gap-12 px-6 pb-20 pt-20 lg:grid-cols-[1.1fr_0.9fr] lg:pt-28">
            <div>
              <Eyebrow>SYNC · Beta</Eyebrow>
              <h1 className="mt-5 text-4xl font-medium leading-[1.08] tracking-tight md:text-5xl lg:text-[3.4rem]">
                질문은 한 번만 답하고,
                <br />
                <span className="text-primary">영원히 찾을 수 있게.</span>
              </h1>
              <p className="mt-6 max-w-md text-base leading-relaxed text-muted-foreground">
                채팅은 지식을 흘려보내고, 위키는 낡아 신뢰를 잃습니다. SYNC는
                팀의 Q&amp;A와 결정을 포럼으로 담아, 검색으로 잘 뜨고 시간이
                지나도 믿을 수 있게 만듭니다.
              </p>
              <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                <LinkButton size="lg" href={ROUTES.REGISTER()}>
                  무료로 시작하기
                </LinkButton>
              </div>
              <p className={cn(MONO, 'mt-6 text-[11px] text-muted-foreground')}>
                5–50명 규모의 기술 팀과 개발자 커뮤니티를 위해 설계되었습니다.
              </p>
            </div>

            <CanonicalCard />
          </div>
        </section>

        {/* Bento */}
        <section className="mx-auto w-full max-w-6xl px-6 pb-24">
          <div className="mb-10 flex flex-col gap-3">
            <Eyebrow>무엇이 다른가</Eyebrow>
            <h2 className="max-w-2xl text-2xl font-medium tracking-tight md:text-3xl">
              지식 도구는 저장이 아니라 신뢰와 검색에서 무너집니다.
              <br className="hidden md:block" /> SYNC는 그 두 가지에 집중합니다.
            </h2>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-6">
            {/* 시그니처: 신선도/신뢰 시스템 */}
            <BentoTile
              className="md:col-span-3 md:row-span-2"
              eyebrow="Freshness & Trust"
              title="6개월 뒤에도 믿을 수 있는 답변"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                모든 정본 지식에는 검증일, 담당자, 그리고 자동으로 계산되는
                노후도가 붙습니다. 오래된 문서는 스스로 표시되고, 담당자는 한
                번의 클릭으로 &ldquo;여전히 맞음&rdquo;을 확인합니다.
              </p>

              <div className="mt-auto space-y-2 pt-6">
                {[
                  {
                    label: 'API 인증 흐름',
                    state: '검증됨 · 2일 전',
                    fresh: true,
                  },
                  {
                    label: '온보딩 체크리스트',
                    state: '검증됨 · 3주 전',
                    fresh: true,
                  },
                  {
                    label: '레거시 배포 스크립트',
                    state: '확인 필요 · 5개월 전',
                    fresh: false,
                  },
                ].map((row) => (
                  <div
                    key={row.label}
                    className="flex items-center justify-between rounded-lg border border-border bg-background px-3 py-2.5"
                  >
                    <span className="text-sm">{row.label}</span>
                    <span
                      className={cn(
                        MONO,
                        'flex items-center gap-1.5 text-[11px]',
                        row.fresh ? 'text-primary' : 'text-amber-600',
                      )}
                    >
                      <span
                        className={cn(
                          'size-1.5 rounded-full',
                          row.fresh ? 'bg-primary' : 'bg-amber-500',
                        )}
                      />
                      {row.state}
                    </span>
                  </div>
                ))}
              </div>
            </BentoTile>

            {/* 검색 & 관련도 */}
            <BentoTile
              className="md:col-span-3"
              eyebrow="Search & Relevance"
              title="오타에 강하고, 관련도로 정렬되는 검색"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                시간순도, 정확히 일치하는 것만도 아닙니다. 필드 가중치와
                트라이그램 유사도로 원하는 답이 먼저 뜹니다.
              </p>
              <div className="mt-4 flex items-center gap-2 rounded-lg border border-border bg-background px-3 py-2">
                <svg
                  viewBox="0 0 24 24"
                  className="size-4 text-muted-foreground"
                  fill="none"
                >
                  <circle
                    cx="11"
                    cy="11"
                    r="7"
                    stroke="currentColor"
                    strokeWidth="2"
                  />
                  <path
                    d="m20 20-3-3"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                  />
                </svg>
                <span className="text-sm text-muted-foreground">
                  디플로이 롤백
                </span>
                <span className={cn(MONO, 'ml-auto text-[10px] text-primary')}>
                  ~12ms
                </span>
              </div>
            </BentoTile>

            {/* 실시간 */}
            <BentoTile
              className="md:col-span-3"
              eyebrow="Real-time"
              title="새로고침 없이 살아 움직이는 지식"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                새 글과 답변이 즉시 반영되고, 누가 보고 있는지, 무엇을 아직 안
                읽었는지 실시간으로 이어집니다.
              </p>
              <div className="mt-4 flex items-center gap-2">
                <div className="flex -space-x-2">
                  {['bg-primary', 'bg-foreground/70', 'bg-amber-500'].map(
                    (c, i) => (
                      <span
                        key={i}
                        className={cn(
                          'size-6 rounded-full border-2 border-card',
                          c,
                        )}
                      />
                    ),
                  )}
                </div>
                <span className={cn(MONO, 'text-[11px] text-muted-foreground')}>
                  3명 접속 중
                </span>
              </div>
            </BentoTile>

            {/* 백링크 */}
            <BentoTile
              className="md:col-span-2"
              eyebrow="Backlinks"
              title="쌓이지 않고 이어지는 지식 그래프"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                어떤 글이든 서로 참조하면 양방향으로 연결됩니다.
              </p>
            </BentoTile>

            {/* 정본 답변 */}
            <BentoTile
              className="md:col-span-2"
              eyebrow="Canonical answers"
              title="흩어진 답을 하나로 수렴"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                최고의 답변을 정본으로 승격해, 같은 질문을 다시 묻지 않게.
              </p>
            </BentoTile>

            {/* 채팅 인제스천 */}
            <BentoTile
              className="md:col-span-2"
              eyebrow="Chat ingestion"
              title="Slack·Discord 스레드를 질문으로"
            >
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                지식이 만들어지는 곳에서, 한 번의 동작으로 옮겨 담습니다.
              </p>
            </BentoTile>
          </div>
        </section>

        {/* 세 가지 글 유형 */}
        <section className="border-y border-border bg-muted/30">
          <div className="mx-auto w-full max-w-6xl px-6 py-20">
            <Eyebrow>세 가지 글 유형</Eyebrow>
            <h2 className="mt-4 max-w-2xl text-2xl font-medium tracking-tight md:text-3xl">
              팀이 이미 하고 있는 대화를, 제자리에 담습니다.
            </h2>
            <div className="mt-10 grid grid-cols-1 gap-4 md:grid-cols-3">
              {[
                {
                  tag: 'SHORT',
                  title: '짧은 글',
                  desc: 'Slack에서 흘러가 버리는 공지와 “참고하세요”를 대신합니다.',
                },
                {
                  tag: 'QUESTION',
                  title: '질문',
                  desc: 'DM과 스레드에서 한 번 답하고 사라지던 Q&A를 붙잡아 둡니다.',
                },
                {
                  tag: 'LONG',
                  title: '긴 글',
                  desc: '위키에 넣자마자 낡기 시작하던 결정과 가이드를 대신합니다.',
                },
              ].map((c) => (
                <div
                  key={c.tag}
                  className="rounded-xl border border-border bg-card p-6"
                >
                  <TypeTag label={c.tag} />
                  <h3 className="mt-4 text-lg font-medium">{c.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                    {c.desc}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 최종 CTA */}
        <section className="mx-auto w-full max-w-6xl px-6 py-24">
          <div className="relative overflow-hidden rounded-2xl border border-border bg-card px-8 py-16 text-center">
            <div
              className="pointer-events-none absolute inset-0 -z-10 opacity-[0.05]"
              style={{
                backgroundImage:
                  'radial-gradient(circle at 50% 0%, var(--color-brand), transparent 60%)',
              }}
            />
            <Eyebrow>지금은 베타</Eyebrow>
            <h2 className="mx-auto mt-5 max-w-xl text-3xl font-medium tracking-tight md:text-4xl">
              팀의 지식이 낡지 않도록.
            </h2>
            <p className="mx-auto mt-4 max-w-md text-muted-foreground">
              가장 먼저 SYNC를 사용해 볼 팀을 찾고 있습니다. 함께 만들어 가요.
            </p>
            <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
              <LinkButton size="lg" href={ROUTES.REGISTER()}>
                베타 시작하기
              </LinkButton>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-border">
        <div className="mx-auto grid w-full max-w-6xl grid-cols-2 gap-8 px-6 py-12 md:grid-cols-4">
          <div className="col-span-2 md:col-span-2">
            <Logo />
            <p className="mt-3 max-w-[260px] text-sm text-muted-foreground">
              한 번 답한 질문이, 6개월 뒤에도 잘 찾히고 신뢰받는 곳.
            </p>
          </div>
          <div>
            <h4
              className={cn(
                MONO,
                'mb-3 text-[11px] uppercase text-muted-foreground',
              )}
            >
              약관
            </h4>
            <ul className="flex flex-col gap-2 text-sm text-muted-foreground">
              <li>
                <Link href={ROUTES.TERMS()} className="hover:text-foreground">
                  이용약관
                </Link>
              </li>
              <li>
                <Link href={ROUTES.PRIVACY()} className="hover:text-foreground">
                  개인정보처리방침
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h4
              className={cn(
                MONO,
                'mb-3 text-[11px] uppercase text-muted-foreground',
              )}
            >
              제품
            </h4>
            <ul className="flex flex-col gap-2 text-sm text-muted-foreground">
              <li>
                <Link
                  href={ROUTES.REGISTER()}
                  className="hover:text-foreground"
                >
                  시작하기
                </Link>
              </li>
              <li>
                <Link href={ROUTES.LOGIN()} className="hover:text-foreground">
                  로그인
                </Link>
              </li>
            </ul>
          </div>
        </div>
        <div className="mx-auto w-full max-w-6xl border-t border-border px-6 py-6">
          <Copyright />
        </div>
      </footer>
    </div>
  );
}
