'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { CheckIcon, PlusIcon } from '@phosphor-icons/react';
import { useQueryClient } from '@tanstack/react-query';
import { useParams } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import {
  getGetProjectTagsQueryKey,
  getGetProjectUnverifiedTagsQueryKey,
  useCreateProjectTag,
  useGetProjectTags,
  useGetProjectUnverifiedTags,
  useVerifyTag,
} from '@/api/__generated__/tag/tag';
import { GetTagsResponseTagsItem } from '@/api/__generated__/types';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import {
  Popover,
  PopoverContent,
  PopoverHeader,
  PopoverTitle,
  PopoverTrigger,
} from '@/components/ui/popover';
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
      <div className="flex justify-end">
        <CreateProjectTagPopover />
      </div>

      <UnverifiedTagsSection />
      <VerifiedTagsSection />
    </div>
  );
}

const CreateProjectTagFormSchema = z.object({
  name: z.string().trim().min(1),
});

type CreateProjectTagFormValues = z.infer<typeof CreateProjectTagFormSchema>;

function CreateProjectTagPopover() {
  const { handle } = useParams<{ handle: string }>();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);

  const form = useForm<CreateProjectTagFormValues>({
    resolver: zodResolver(CreateProjectTagFormSchema),
    defaultValues: { name: '' },
  });

  const { mutate: createProjectTag, isPending } = useCreateProjectTag();

  const onSubmit = form.handleSubmit((values) => {
    createProjectTag(
      { handle, data: { name: values.name } },
      {
        onSuccess: async (response) => {
          toast.success(`'${response.data.name}' 태그를 생성했습니다.`);
          await queryClient.invalidateQueries({
            queryKey: getGetProjectTagsQueryKey(handle),
          });
          form.reset();
          setOpen(false);
        },
        onError: () => {
          toast.error('태그 생성에 실패했습니다.');
        },
      },
    );
  });

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button size="sm">
          <PlusIcon className="h-4 w-4" />새 태그
        </Button>
      </PopoverTrigger>

      <PopoverContent align="end">
        <PopoverHeader>
          <PopoverTitle>새 태그 생성</PopoverTitle>
        </PopoverHeader>

        <form onSubmit={onSubmit} className="flex flex-col gap-3">
          <FieldGroup>
            <Field>
              <FieldLabel>태그 이름</FieldLabel>
              <Input {...form.register('name')} placeholder="예: java" />
            </Field>
          </FieldGroup>

          <div className="flex justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                form.reset();
                setOpen(false);
              }}
            >
              취소
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={!form.formState.isDirty || isPending}
            >
              생성
            </Button>
          </div>
        </form>
      </PopoverContent>
    </Popover>
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
