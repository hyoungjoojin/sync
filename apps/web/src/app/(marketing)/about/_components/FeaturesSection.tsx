import { getTranslations } from 'next-intl/server';

import { APP_NAME } from '@/constants/app';
import longPostImage from '@/public/assets/features/post-type-long.png';
import questionPostImage from '@/public/assets/features/post-type-question.png';
import shortPostImage from '@/public/assets/features/post-type-short.png';

import BentoTile from './BentoTile';
import CollectionDeck from './CollectionDeck';
import EditorTypingPreview, {
  type CodeToken,
  type EditorCommand,
  type EditorScriptStep,
} from './EditorTypingPreview';
import PostTypeCarousel, { type PostTypeSlide } from './PostTypeCarousel';
import ProjectSpaceMorph from './ProjectSpaceMorph';
import SearchTypingPreview, { type SearchResult } from './SearchTypingPreview';

const POST_TYPE_KEYS = ['short', 'question', 'long'] as const;

const POST_TYPE_IMAGES = {
  short: shortPostImage,
  question: questionPostImage,
  long: longPostImage,
} as const;

const EDITOR_COMMAND_KEYS = ['heading', 'code', 'todo', 'quote'] as const;

const EDITOR_PICKED_COMMAND = EDITOR_COMMAND_KEYS.indexOf('code');

const SEARCH_RESULT_KEYS = ['first'] as const;

/** 3. 기능 벤토 그리드. */
export default async function FeaturesSection() {
  const t = await getTranslations('pages.about.bento');
  const tPostType = await getTranslations('components.post.type');

  const postTypeSlides: PostTypeSlide[] = POST_TYPE_KEYS.map((key) => ({
    key,
    tag: key.toUpperCase(),
    name: tPostType(key.toUpperCase() as 'SHORT' | 'QUESTION' | 'LONG'),
    description: t(`items.postTypes.${key}`),
    image: POST_TYPE_IMAGES[key],
    alt: t(`items.postTypes.alt.${key}`),
  }));

  const editorCommands: EditorCommand[] = EDITOR_COMMAND_KEYS.map((key) => ({
    key,
    label: t(`items.editor.commands.${key}`),
  }));

  const searchResults: SearchResult[] = SEARCH_RESULT_KEYS.map((key) => ({
    key,
    title: t(`items.search.results.${key}`),
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
      className="mx-auto flex w-full max-w-6xl scroll-mt-20 flex-col px-6 py-14 md:h-[52rem] md:py-6"
    >
      <h2 className="shrink-0 text-3xl font-bold tracking-tight md:text-4xl">
        {t('title', { appName: APP_NAME })}
      </h2>

      <div className="mt-6 grid min-h-0 flex-1 grid-cols-1 gap-4 md:grid-cols-12 md:grid-rows-[minmax(0,0.95fr)_minmax(0,1.05fr)]">
        <BentoTile
          tone="deepest"
          className="md:col-span-7"
          title={t('items.postTypes.title')}
          description={t('items.postTypes.description')}
        >
          <PostTypeCarousel slides={postTypeSlides} />
        </BentoTile>

        <BentoTile
          tone="deep"
          className="md:col-span-5"
          title={t('items.editor.title')}
          description={t('items.editor.description')}
        >
          <EditorTypingPreview
            script={editorScript}
            commands={editorCommands}
          />
        </BentoTile>

        <div className="flex min-h-0 flex-col gap-4 md:col-span-4">
          <BentoTile
            tone="mid"
            className="flex-1 p-4"
            title={t('items.search.title')}
            description={t('items.search.description')}
          >
            <SearchTypingPreview
              query={t('items.search.query')}
              results={searchResults}
            />
          </BentoTile>

          <BentoTile
            tone="soft"
            className="relative isolate min-h-32 flex-1 p-4"
            title={t('items.collections.title')}
            description={t('items.collections.description')}
          >
            <CollectionDeck />
          </BentoTile>
        </div>

        <BentoTile
          tone="softer"
          className="md:col-span-8"
          title={t('items.projects.title')}
          description={t('items.projects.description')}
        >
          <ProjectSpaceMorph />
        </BentoTile>
      </div>
    </section>
  );
}
