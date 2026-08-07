'use client';

import { useTranslations } from 'next-intl';
import { useState } from 'react';

import { useActivePromotions } from '@/components/feature/promotion/hooks/useActivePromotions';
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useModal } from '@/hooks/store';

import PromotionCard from './_components/PromotionCard';

export default function PromotionsModal() {
  const t = useTranslations('modals.promotions');
  const { isOpen, closeModal } = useModal();

  const { data, isPending } = useActivePromotions();
  const promotions = data?.data ?? [];

  // ModalProvider unmounts this component entirely when the modal closes, so
  // this initial value is naturally fresh every time it opens again.
  const [activeTab, setActiveTab] = useState('0');

  return (
    <Dialog
      open={isOpen}
      onOpenChange={(open) => {
        if (!open) {
          closeModal();
        }
      }}
    >
      <DialogContent className="sm:max-w-3xl">
        <DialogHeader>
          <div className="flex items-start justify-between gap-4">
            <div className="flex min-w-0 flex-col gap-2">
              <DialogTitle>{t('title')}</DialogTitle>
              <DialogDescription>{t('description')}</DialogDescription>
            </div>
            <DialogClose />
          </div>
        </DialogHeader>

        {isPending ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-24 w-full" />
          </div>
        ) : promotions.length === 0 ? (
          <p className="text-muted-foreground py-8 text-center text-sm">
            {t('empty')}
          </p>
        ) : (
          <Tabs
            value={Number(activeTab) < promotions.length ? activeTab : '0'}
            onValueChange={setActiveTab}
          >
            {promotions.length > 1 && (
              <div className="flex justify-center">
                <TabsList>
                  {promotions.map((promotion, index) => (
                    <TabsTrigger key={promotion.id} value={String(index)}>
                      {index + 1}
                    </TabsTrigger>
                  ))}
                </TabsList>
              </div>
            )}

            {promotions.map((promotion, index) => (
              <TabsContent
                key={promotion.id}
                value={String(index)}
                className="max-h-[80vh] overflow-y-auto px-4"
              >
                <PromotionCard promotion={promotion} />
              </TabsContent>
            ))}
          </Tabs>
        )}
      </DialogContent>
    </Dialog>
  );
}
