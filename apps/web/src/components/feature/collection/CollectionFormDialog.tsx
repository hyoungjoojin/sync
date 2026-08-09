'use client';

import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import SyncError, { ErrorCode } from '@/lib/error';

import { useCreateCollection } from './hooks/useCreateCollection';
import { useCreateProjectCollection } from './hooks/useCreateProjectCollection';
import { useUpdateCollection } from './hooks/useUpdateCollection';

interface CollectionInitialValues {
  externalId: string;
  name: string;
  description?: string | null;
  isPublic: boolean;
}

interface CollectionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** 지정하면 수정 모드, 없으면 생성 모드. */
  collection?: CollectionInitialValues;
  /**
   * 생성 모드에서 지정하면 해당 프로젝트의 컬렉션으로 생성한다. 없으면 개인
   * 컬렉션. (수정 모드에서는 무시된다 — 수정 엔드포인트는 스코프 공통.)
   */
  projectHandle?: string;
  /** 생성 완료 후 새 컬렉션의 externalId 를 전달한다. */
  onCreated?: (externalId: string) => void;
}

export function CollectionFormDialog({
  open,
  onOpenChange,
  collection,
  projectHandle,
  onCreated,
}: CollectionFormDialogProps) {
  const t = useTranslations('pages.collections.form');
  const isEdit = !!collection;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {isEdit ? t('title.edit') : t('title.create')}
          </DialogTitle>
        </DialogHeader>

        {/* DialogContent 는 열릴 때만 마운트되므로, 폼 상태는 props 로부터
            직접 초기화한다 (effect 로 동기화하지 않는다). */}
        <CollectionForm
          collection={collection}
          projectHandle={projectHandle}
          onClose={() => onOpenChange(false)}
          onCreated={onCreated}
        />
      </DialogContent>
    </Dialog>
  );
}

function CollectionForm({
  collection,
  projectHandle,
  onClose,
  onCreated,
}: {
  collection?: CollectionInitialValues;
  projectHandle?: string;
  onClose: () => void;
  onCreated?: (externalId: string) => void;
}) {
  const t = useTranslations('pages.collections.form');
  const isEdit = !!collection;

  const [name, setName] = useState(collection?.name ?? '');
  const [description, setDescription] = useState(collection?.description ?? '');
  const [isPublic, setIsPublic] = useState(collection?.isPublic ?? true);

  const { mutate: createCollection, isPending: isCreatingPersonal } =
    useCreateCollection();
  const { mutate: createProjectCollection, isPending: isCreatingProject } =
    useCreateProjectCollection();
  const { mutate: updateCollection, isPending: isUpdating } =
    useUpdateCollection();

  const isCreating = isCreatingPersonal || isCreatingProject;

  const submit = () => {
    const trimmedName = name.trim();
    if (!trimmedName) {
      toast.error(t('messages.name-required'));
      return;
    }

    const payload = {
      name: trimmedName,
      description: description.trim() || null,
      isPublic,
    };

    if (collection) {
      updateCollection(
        { externalId: collection.externalId, data: payload },
        {
          onSuccess: () => {
            toast.success(t('messages.update-success'));
            onClose();
          },
          onError: (error) => {
            if (
              error instanceof SyncError &&
              error.code === ErrorCode.COLLECTION_NOT_FOUND
            ) {
              toast.error(t('messages.update-error-not-found'));
              return;
            }
            toast.error(t('messages.error'));
          },
        },
      );
      return;
    }

    const onCreateSuccess = (response: { data: { externalId: string } }) => {
      toast.success(t('messages.create-success'));
      onClose();
      onCreated?.(response.data.externalId);
    };
    const onError = () => toast.error(t('messages.error'));

    if (projectHandle) {
      createProjectCollection(
        { handle: projectHandle, data: payload },
        { onSuccess: onCreateSuccess, onError },
      );
      return;
    }

    createCollection(
      { data: payload },
      { onSuccess: onCreateSuccess, onError },
    );
  };

  return (
    <>
      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="collection-name">{t('fields.name')}</Label>
          <Input
            id="collection-name"
            value={name}
            maxLength={255}
            onChange={(event) => setName(event.target.value)}
            placeholder={t('fields.name-placeholder')}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="collection-description">
            {t('fields.description')}
          </Label>
          <Textarea
            id="collection-description"
            value={description}
            maxLength={1000}
            onChange={(event) => setDescription(event.target.value)}
            placeholder={t('fields.description-placeholder')}
          />
        </div>

        <div className="flex items-center gap-2">
          <Checkbox
            id="collection-public"
            checked={isPublic}
            onCheckedChange={(checked) => setIsPublic(checked === true)}
          />
          <Label htmlFor="collection-public" className="font-normal">
            {t('fields.public')}
          </Label>
        </div>
      </div>

      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>
          {t('actions.cancel')}
        </Button>
        <Button
          type="button"
          isPending={isCreating || isUpdating}
          onClick={submit}
        >
          {isEdit ? t('actions.save') : t('actions.create')}
        </Button>
      </DialogFooter>
    </>
  );
}
