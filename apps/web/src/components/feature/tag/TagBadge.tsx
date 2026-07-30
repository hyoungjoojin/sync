'use client';

import { FolderSimpleIcon, PlusIcon, XIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { ComponentProps, ReactElement } from 'react';

import { Badge } from '@/components/ui/badge';
import { Spinner } from '@/components/ui/spinner';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import { cn } from '@/lib/utils';

interface TagBadgeProps extends ComponentProps<typeof Badge> {
  name: string;
  isProjectTag?: boolean;
  description?: string;
}

function ProjectTagIndicator({ withTooltip }: { withTooltip: boolean }) {
  const t = useTranslations('components.editor.tags');

  if (!withTooltip) {
    return <FolderSimpleIcon weight="fill" />;
  }

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <FolderSimpleIcon weight="fill" />
      </TooltipTrigger>
      <TooltipContent>{t('project-tag')}</TooltipContent>
    </Tooltip>
  );
}

function DescriptionTooltip({
  description,
  children,
}: {
  description?: string;
  children: ReactElement;
}) {
  if (!description) {
    return children;
  }

  return (
    <Tooltip>
      <TooltipTrigger asChild>{children}</TooltipTrigger>
      <TooltipContent>{description}</TooltipContent>
    </Tooltip>
  );
}

export function TagBadge({
  name,
  isProjectTag,
  description,
  ...props
}: TagBadgeProps) {
  return (
    <DescriptionTooltip description={description}>
      <Badge {...props}>
        {isProjectTag && <ProjectTagIndicator withTooltip={!description} />}
        {name}
      </Badge>
    </DescriptionTooltip>
  );
}

interface RemovableTagBadgeProps extends TagBadgeProps {
  onRemove: () => void;
}

export function RemovableTagBadge({
  name,
  isProjectTag,
  description,
  onRemove,
  variant = 'secondary',
  className,
  ...props
}: RemovableTagBadgeProps) {
  return (
    <DescriptionTooltip description={description}>
      <button
        type="button"
        onClick={(event) => {
          event.stopPropagation();
          onRemove();
        }}
      >
        <Badge variant={variant} className={cn('gap-1', className)} {...props}>
          {isProjectTag && <ProjectTagIndicator withTooltip={!description} />}
          {name}
          <XIcon size={10} />
        </Badge>
      </button>
    </DescriptionTooltip>
  );
}

interface SelectableTagBadgeProps extends TagBadgeProps {
  onSelect: () => void;
  isPending?: boolean;
  disabled?: boolean;
}

export function SelectableTagBadge({
  name,
  isProjectTag,
  description,
  onSelect,
  isPending = false,
  disabled = false,
  variant = 'default',
  className,
  ...props
}: SelectableTagBadgeProps) {
  return (
    <DescriptionTooltip description={description}>
      <button
        type="button"
        aria-disabled={disabled || isPending}
        onClick={(event) => {
          event.stopPropagation();
          if (disabled || isPending) {
            return;
          }
          onSelect();
        }}
        className="transition-all hover:cursor-pointer active:scale-[.98] aria-disabled:cursor-not-allowed aria-disabled:opacity-50 aria-disabled:active:scale-100"
      >
        <Badge variant={variant} className={cn('gap-1', className)} {...props}>
          {isProjectTag && <ProjectTagIndicator withTooltip={!description} />}
          {name}
          {isPending ? (
            <Spinner className="size-2.5 border-current" />
          ) : (
            <PlusIcon size={10} />
          )}
        </Badge>
      </button>
    </DescriptionTooltip>
  );
}
