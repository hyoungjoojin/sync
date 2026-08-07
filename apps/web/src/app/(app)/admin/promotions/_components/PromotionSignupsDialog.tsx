'use client';

import { useTranslations } from 'next-intl';

import { useGetPromotionSignups } from '@/api/__generated__/promotion/promotion';
import type { PromotionResponseListItem } from '@/api/__generated__/types/PromotionResponseListItem';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

interface PromotionSignupsDialogProps {
  promotion: PromotionResponseListItem | null;
  onOpenChange: (open: boolean) => void;
}

export default function PromotionSignupsDialog({
  promotion,
  onOpenChange,
}: PromotionSignupsDialogProps) {
  const t = useTranslations('pages.admin.promotions');

  const { data, isPending } = useGetPromotionSignups(
    String(promotion?.id ?? ''),
    {
      query: { enabled: promotion !== null },
    },
  );
  const signups = data?.data ?? [];

  return (
    <Dialog open={promotion !== null} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {t('signups-dialog.title', { title: promotion?.postTitle ?? '' })}
          </DialogTitle>
        </DialogHeader>

        {isPending ? (
          <Skeleton className="h-48 w-full" />
        ) : (
          <div className="max-h-[70vh] overflow-y-auto px-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>{t('signups-dialog.table.user')}</TableHead>
                  <TableHead>{t('signups-dialog.table.attachment')}</TableHead>
                  <TableHead>{t('signups-dialog.table.createdAt')}</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {signups.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} className="h-24 text-center">
                      {t('signups-dialog.empty')}
                    </TableCell>
                  </TableRow>
                ) : (
                  signups.map((signup) => (
                    <TableRow key={signup.id}>
                      <TableCell>
                        <div className="flex flex-col">
                          <span>{signup.user.name}</span>
                          <span className="text-muted-foreground text-xs">
                            @{signup.user.handle}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col gap-1">
                          {(promotion?.fields ?? []).map((field) => {
                            const attachment = signup.attachment as Record<
                              string,
                              string | null | undefined
                            >;
                            const value = attachment[field.key];
                            if (value === undefined || value === null) {
                              return null;
                            }

                            return (
                              <span key={field.key} className="text-sm">
                                {field.label}: {value}
                              </span>
                            );
                          })}
                        </div>
                      </TableCell>
                      <TableCell>{formatDate(signup.createdAt)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}
