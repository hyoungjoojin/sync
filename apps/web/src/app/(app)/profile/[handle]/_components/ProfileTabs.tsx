'use client';

import { useTranslations } from 'next-intl';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';

import { useGetProfileByHandle } from '@/api/__generated__/profile/profile';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useSession } from '@/lib/auth/client';

import ProfileBookmarks from './ProfileBookmarks';
import ProfileCollections from './ProfileCollections';
import ProfileDrafts from './ProfileDrafts';
import ProfileLikes from './ProfileLikes';
import ProfilePosts from './ProfilePosts';
import ProfileQuestions from './ProfileQuestions';

interface ProfileTabsProps {
  handle: string;
}

const SELF_ONLY_TABS = ['drafts', 'bookmarks', 'likes'];
const ALL_TABS = [
  'posts',
  'questions',
  'collections',
  'drafts',
  'bookmarks',
  'likes',
];

export default function ProfileTabs({ handle }: ProfileTabsProps) {
  const t = useTranslations('pages.profile');
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const { data: session } = useSession();
  const { data: profile } = useGetProfileByHandle(handle);

  const isOwnProfile =
    !!session?.user.id &&
    !!profile?.data.userId &&
    String(session.user.id) === String(profile.data.userId);

  const requestedTab = searchParams.get('tab');
  const tab =
    requestedTab &&
    ALL_TABS.includes(requestedTab) &&
    (isOwnProfile || !SELF_ONLY_TABS.includes(requestedTab))
      ? requestedTab
      : 'posts';

  const tabChangeHandler = (value: string) => {
    const params = new URLSearchParams(searchParams.toString());
    if (value === 'posts') {
      params.delete('tab');
    } else {
      params.set('tab', value);
    }
    const query = params.toString();
    router.replace(query ? `${pathname}?${query}` : pathname, {
      scroll: false,
    });
  };

  return (
    <Tabs value={tab} onValueChange={tabChangeHandler}>
      <TabsList variant="line">
        <TabsTrigger value="posts">{t('posts.label')}</TabsTrigger>
        <TabsTrigger value="questions">{t('tabs.questions.label')}</TabsTrigger>
        <TabsTrigger value="collections">
          {t('tabs.collections.label')}
        </TabsTrigger>
        {isOwnProfile && (
          <>
            <TabsTrigger value="drafts">{t('tabs.drafts.label')}</TabsTrigger>
            <TabsTrigger value="bookmarks">
              {t('tabs.bookmarks.label')}
            </TabsTrigger>
            <TabsTrigger value="likes">{t('tabs.likes.label')}</TabsTrigger>
          </>
        )}
      </TabsList>

      <TabsContent value="posts">
        <ProfilePosts handle={handle} />
      </TabsContent>

      <TabsContent value="questions">
        <ProfileQuestions handle={handle} />
      </TabsContent>

      <TabsContent value="collections">
        <ProfileCollections handle={handle} />
      </TabsContent>

      {isOwnProfile && (
        <>
          <TabsContent value="drafts">
            <ProfileDrafts />
          </TabsContent>

          <TabsContent value="bookmarks">
            <ProfileBookmarks />
          </TabsContent>

          <TabsContent value="likes">
            <ProfileLikes />
          </TabsContent>
        </>
      )}
    </Tabs>
  );
}
