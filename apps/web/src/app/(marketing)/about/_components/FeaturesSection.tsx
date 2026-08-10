import { getTranslations } from 'next-intl/server';

import BentoTile from './BentoTile';
import EditorTypingPreview, {
  type CodeToken,
  type EditorCommand,
  type EditorScriptStep,
} from './EditorTypingPreview';
import MoreFeaturesList from './MoreFeaturesList';
import PostShapeCarousel, { type ShapeExample } from './PostShapeCarousel';
import ProjectSpaceMorph from './ProjectSpaceMorph';
import RevealProvider from './Reveal';

const EDITOR_COMMAND_KEYS = ['heading', 'code', 'todo', 'quote'] as const;

const EDITOR_PICKED_COMMAND = EDITOR_COMMAND_KEYS.indexOf('code');

// 벤토 배치는 6열이 다 있어야 성립한다. 2열로 접으면 마지막 타일이 반 칸만 채워
// 빈자리가 남으므로, 중간 단계 없이 lg 미만에서는 한 줄에 한 장씩 쌓는다.
const SPAN_4 = 'lg:col-span-4';

const SPAN_3 = 'lg:col-span-3';

const SPAN_2 = 'lg:col-span-2';

/** 3. sync 소개 — 벤토 그리드. */
export default async function FeaturesSection() {
  const t = await getTranslations('pages.about.bento');

  const editorCommands: EditorCommand[] = EDITOR_COMMAND_KEYS.map((key) => ({
    key,
    label: t(`items.editor.commands.${key}`),
  }));

  const editorScript: EditorScriptStep[] = [
    { kind: 'heading', text: t('items.editor.doc.heading') },
    { kind: 'menu', pick: EDITOR_PICKED_COMMAND },
    {
      kind: 'code',
      language: t('items.editor.doc.code.language'),
      tokens: [
        { text: t('items.editor.doc.code.command'), token: 'built_in' },
        { text: t('items.editor.doc.code.arguments') },
        { text: t('items.editor.doc.code.target'), token: 'string' },
      ] satisfies CodeToken[],
    },
    { kind: 'todo', text: t('items.editor.doc.todo') },
  ];

  return (
    <section
      id="features"
      className="mx-auto w-full max-w-[1200px] scroll-mt-20 px-6 py-14 md:py-20"
    >
      <div className="mx-auto max-w-2xl text-center">
        <h2 className="text-3xl font-medium tracking-tight text-muted-foreground md:text-4xl">
          {t('heading')}
        </h2>
        <p className="mt-3 text-5xl font-medium leading-tight tracking-tight md:text-6xl">
          {t('sub')}
        </p>
      </div>

      <div className="mt-14 grid grid-cols-1 gap-4 lg:grid-cols-6 lg:grid-rows-[380px_380px]">
        <RevealProvider index={0} className={SPAN_3}>
          <BentoTile
            tone="deepest"
            className="h-full"
            title={t('items.shapes.title')}
            description={t('items.shapes.desc')}
          >
            <PostShapeCarousel
              examples={t.raw('items.shapes.examples') as ShapeExample[]}
              resolvedBadge={t('items.shapes.resolvedBadge')}
            />
          </BentoTile>
        </RevealProvider>

        <RevealProvider index={1} className={SPAN_3}>
          <BentoTile
            tone="deep"
            className="h-full"
            title={t('items.editor.title')}
            description={t('items.editor.desc')}
          >
            <EditorTypingPreview
              script={editorScript}
              commands={editorCommands}
            />
          </BentoTile>
        </RevealProvider>

        <RevealProvider index={2} className={SPAN_4}>
          <BentoTile
            tone="mid"
            className="h-full"
            title={t('items.projects.title')}
            description={t('items.projects.desc')}
          >
            <ProjectSpaceMorph />
          </BentoTile>
        </RevealProvider>

        <RevealProvider index={3} className={SPAN_2}>
          <BentoTile
            tone="soft"
            className="h-full"
            title={t('items.more.title')}
            description={t('items.more.desc')}
          >
            <MoreFeaturesList items={t.raw('items.more.items') as string[]} />
          </BentoTile>
        </RevealProvider>
      </div>
    </section>
  );
}
