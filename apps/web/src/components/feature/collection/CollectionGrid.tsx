'use client';

import {
  GlobeIcon,
  LockIcon,
  PlusIcon,
  StackSimpleIcon,
} from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import Link from 'next/link';
import { useState } from 'react';

import type { GetCollectionsResponseCollectionsItem } from '@/api/__generated__/types/GetCollectionsResponseCollectionsItem';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from '@/components/ui/empty';
import { Skeleton } from '@/components/ui/skeleton';
import ROUTES from '@/util/routes';

import { CollectionFormDialog } from './CollectionFormDialog';

interface CollectionGridProps {
  title: string;
  description: string;
  collections: GetCollectionsResponseCollectionsItem[];
  isPending: boolean;
  /** 생성 버튼 노출 여부 (개인은 항상 true, 프로젝트는 관리자만). */
  canCreate: boolean;
  /** 지정하면 생성 시 해당 프로젝트의 컬렉션으로 만든다. */
  projectHandle?: string;
}

/**
 * 개인/프로젝트 컬렉션 목록에서 공유하는 프레젠테이션 컴포넌트. 데이터 조회는
 * 하지 않고 넘겨받은 컬렉션 배열만 렌더링한다.
 */
export function CollectionGrid({
  title,
  description,
  collections,
  isPending,
  canCreate,
  projectHandle,
}: CollectionGridProps) {
  const t = useTranslations('pages.collections');
  const [createOpen, setCreateOpen] = useState(false);

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold">{title}</h1>
          <p className="text-muted-foreground text-sm">{description}</p>
        </div>

        {canCreate && (
          <Button onClick={() => setCreateOpen(true)}>
            <PlusIcon />
            {t('actions.create')}
          </Button>
        )}
      </div>

      {isPending ? (
        <div className="grid gap-4 sm:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <Skeleton key={index} className="h-28 w-full" />
          ))}
        </div>
      ) : collections.length === 0 ? (
        <Empty className="min-h-80">
          <EmptyMedia variant="icon">
            <StackSimpleIcon />
          </EmptyMedia>
          <EmptyHeader>
            <EmptyTitle>{t('list-empty.title')}</EmptyTitle>
            <EmptyDescription>{t('list-empty.description')}</EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {collections.map((collection) => (
            <Link
              key={collection.externalId}
              href={ROUTES.COLLECTION(collection.externalId)}
            >
              <Card className="hover:border-primary/50 h-full transition">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <span className="truncate">{collection.name}</span>
                    <Badge variant="secondary" className="shrink-0 gap-1">
                      {collection.isPublic ? (
                        <GlobeIcon className="size-3" />
                      ) : (
                        <LockIcon className="size-3" />
                      )}
                      {collection.isPublic
                        ? t('visibility.public')
                        : t('visibility.private')}
                    </Badge>
                  </CardTitle>
                </CardHeader>
                <CardContent className="text-muted-foreground space-y-1 text-sm">
                  {collection.description && (
                    <p className="line-clamp-2">{collection.description}</p>
                  )}
                  <p>{t('post-count', { count: collection.postCount })}</p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {canCreate && (
        <CollectionFormDialog
          open={createOpen}
          onOpenChange={setCreateOpen}
          projectHandle={projectHandle}
        />
      )}
    </div>
  );
}
