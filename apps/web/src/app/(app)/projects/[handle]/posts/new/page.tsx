'use client';

import {
  redirect,
  useParams,
  useRouter,
  useSearchParams,
} from 'next/navigation';

import { useCreatePost } from '@/api/__generated__/post/post';
import { useGetProjectByHandle } from '@/api/__generated__/project/project';
import PostEditor from '@/components/feature/post/editor/PostEditor';
import { PostScope, PostType } from '@/components/feature/post/types/post';
import { isAuthenticated, isOnboarded } from '@/lib/auth';
import { useSession } from '@/lib/auth/client';
import ROUTES from '@/util/routes';

function getInitialPostType(value: string | null): PostType {
  if (value === PostType.SHORT || value === PostType.QUESTION) {
    return value;
  }

  return PostType.LONG;
}

export default function CreateProjectPostPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { handle } = useParams<{ handle: string }>();
  const { data: session, isPending } = useSession();
  const { data: projectData } = useGetProjectByHandle(handle);

  const { mutate: createPost, isPending: isCreatingPost } = useCreatePost({
    mutation: {
      onSuccess: ({ data }) => {
        router.push(ROUTES.PROJECT_POST(handle, data.slug));
      },
    },
  });

  if (!isPending) {
    if (isAuthenticated(session) && !isOnboarded(session)) {
      redirect(ROUTES.ONBOARDING());
    }

    if (!isAuthenticated(session)) {
      redirect(ROUTES.LOGIN());
    }
  }

  return (
    <PostEditor
      type={getInitialPostType(searchParams.get('type'))}
      scope={PostScope.WORKSPACE}
      isSubmitting={isCreatingPost || !projectData}
      project={
        projectData
          ? { handle, name: projectData.data.summary.name }
          : undefined
      }
      onSubmit={({ title, type, scope, status, tags, project, content }) => {
        createPost({
          data: {
            type,
            scope,
            status,
            title,
            tags,
            project,
            content: {
              json: content.json,
              text: content.text,
              mediaIds: content.media.map((media) => media.id),
            },
          },
        });
      }}
    />
  );
}
