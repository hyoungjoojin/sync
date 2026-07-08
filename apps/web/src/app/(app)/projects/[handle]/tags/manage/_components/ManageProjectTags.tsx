'use client';

import { CheckIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useParams } from 'next/navigation';
import { toast } from 'sonner';

import {
  getGetProjectTagsQueryKey,
  getGetProjectUnverifiedTagsQueryKey,
  useGetProjectTags,
  useGetProjectUnverifiedTags,
  useVerifyTag,
} from '@/api/__generated__/tag/tag';
import { GetTagsResponseTagsItem } from '@/api/__generated__/types';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

export default function ManageProjectTags() {
  return (
    <div className="space-y-8">
      <UnverifiedTagsSection />
      <VerifiedTagsSection />
    </div>
  );
}

function UnverifiedTagsSection() {
  const { handle } = useParams<{ handle: string }>();
  const queryClient = useQueryClient();

  const { data, isPending } = useGetProjectUnverifiedTags(handle);
  const tags = data?.data.tags ?? [];

  const { mutate: verifyTag, isPending: isVerifyPending } = useVerifyTag();

  const onVerify = (tag: GetTagsResponseTagsItem) => {
    verifyTag(
      { tagId: tag.id.toString() },
      {
        onSuccess: async () => {
          toast.success(`'${tag.name}' 태그를 인증했습니다.`);
          await queryClient.invalidateQueries({
            queryKey: getGetProjectUnverifiedTagsQueryKey(handle),
          });
          await queryClient.invalidateQueries({
            queryKey: getGetProjectTagsQueryKey(handle),
          });
        },
        onError: () => {
          toast.error('태그 인증에 실패했습니다.');
        },
      },
    );
  };

  return (
    <section className="space-y-3">
      <div>
        <h2 className="text-base font-semibold">인증되지 않은 태그</h2>
        <p className="text-sm text-muted-foreground">
          검토 후 인증할 태그를 선택하세요.
        </p>
      </div>

      {isPending ? (
        <TagsTableSkeleton />
      ) : tags.length === 0 ? (
        <div className="rounded-md border px-4 py-8 text-center">
          <p className="text-sm text-muted-foreground">
            인증되지 않은 태그가 없습니다.
          </p>
        </div>
      ) : (
        <Table className="border-separate border-spacing-y-1">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="border-l-0">이름</TableHead>
              <TableHead className="border-l-0">설명</TableHead>
              <TableHead className="border-l-0">게시물 수</TableHead>
              <TableHead className="w-0 border-l-0" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {tags.map((tag) => (
              <TableRow key={tag.id} className="border-0">
                <TableCell className="border-l-0 font-medium">
                  {tag.name}
                </TableCell>
                <TableCell className="border-l-0 text-muted-foreground">
                  {tag.description || '-'}
                </TableCell>
                <TableCell className="border-l-0">{tag.postCount}</TableCell>
                <TableCell className="border-l-0">
                  <div className="flex justify-end">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={isVerifyPending}
                      onClick={() => onVerify(tag)}
                    >
                      <CheckIcon className="h-4 w-4" />
                      인증
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </section>
  );
}

function VerifiedTagsSection() {
  const { handle } = useParams<{ handle: string }>();
  const { data, isPending } = useGetProjectTags(handle);
  const tags = data?.data.tags ?? [];

  return (
    <section className="space-y-3">
      <div>
        <h2 className="text-base font-semibold">인증된 태그</h2>
        <p className="text-sm text-muted-foreground">
          현재 사용 중인 인증된 태그 목록입니다.
        </p>
      </div>

      {isPending ? (
        <TagsTableSkeleton />
      ) : tags.length === 0 ? (
        <div className="rounded-md border px-4 py-8 text-center">
          <p className="text-sm text-muted-foreground">
            인증된 태그가 없습니다.
          </p>
        </div>
      ) : (
        <Table className="border-separate border-spacing-y-1">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="border-l-0">이름</TableHead>
              <TableHead className="border-l-0">설명</TableHead>
              <TableHead className="border-l-0">게시물 수</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tags.map((tag) => (
              <TableRow key={tag.id} className="border-0">
                <TableCell className="border-l-0 font-medium">
                  <div className="flex items-center gap-2">
                    {tag.name}
                    <Badge variant="secondary">인증됨</Badge>
                  </div>
                </TableCell>
                <TableCell className="border-l-0 text-muted-foreground">
                  {tag.description || '-'}
                </TableCell>
                <TableCell className="border-l-0">{tag.postCount}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </section>
  );
}

function TagsTableSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="h-10 w-full rounded bg-muted animate-pulse" />
      ))}
    </div>
  );
}
