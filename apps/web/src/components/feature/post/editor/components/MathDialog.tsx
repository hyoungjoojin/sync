'use client';

import katex from 'katex';
import { useTranslations } from 'next-intl';
import { useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';

import { NodeType } from '../extensions/nodes';
import type { MathTarget } from '../extensions/nodes/math';

interface MathDialogProps {
  target: MathTarget | null;
  onSubmit: (target: MathTarget, latex: string) => void;
  onDelete: (target: MathTarget) => void;
  onClose: () => void;
}

export function MathDialog({
  target,
  onSubmit,
  onDelete,
  onClose,
}: MathDialogProps) {
  if (target === null) {
    return null;
  }

  // 대상이 바뀌면 입력 상태를 초기화해야 하므로, 대상마다 다른 컴포넌트로 취급한다.
  return (
    <MathDialogContent
      key={`${target.type}:${target.pos}:${String(target.isNew)}`}
      target={target}
      onSubmit={onSubmit}
      onDelete={onDelete}
      onClose={onClose}
    />
  );
}

interface MathDialogContentProps extends Omit<MathDialogProps, 'target'> {
  target: MathTarget;
}

function MathDialogContent({
  target,
  onSubmit,
  onDelete,
  onClose,
}: MathDialogContentProps) {
  const t = useTranslations('components.editor.math');
  const [latex, setLatex] = useState(target.latex);

  const isBlock = target.type === NodeType.BlockMath;
  const trimmed = latex.trim();

  const handleSubmit = () => {
    if (trimmed.length === 0) {
      return;
    }

    onSubmit(target, trimmed);
  };

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {isBlock ? t('title-block') : t('title-inline')}
          </DialogTitle>
          <DialogDescription>{t('description')}</DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-3">
          <Textarea
            autoFocus
            value={latex}
            onChange={(event) => setLatex(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && (event.metaKey || event.ctrlKey)) {
                event.preventDefault();
                handleSubmit();
              }
            }}
            placeholder={t('placeholder')}
            className="min-h-24 font-mono"
            spellCheck={false}
          />

          <MathPreview latex={trimmed} displayMode={isBlock} />
        </div>

        <DialogFooter className="sm:justify-between">
          {target.isNew ? (
            <span />
          ) : (
            <Button
              type="button"
              variant="ghost"
              className="text-destructive"
              onClick={() => onDelete(target)}
            >
              {t('actions.delete')}
            </Button>
          )}

          <div className="flex gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('actions.cancel')}
            </Button>
            <Button
              type="button"
              disabled={trimmed.length === 0}
              onClick={handleSubmit}
            >
              {target.isNew ? t('actions.insert') : t('actions.update')}
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

interface MathPreviewProps {
  latex: string;
  displayMode: boolean;
}

/**
 * 작성 중인 LaTeX 을 그대로 KaTeX 에 넘겨 결과와 오류를 함께 보여준다. 본문 렌더링과
 * 달리 오류 메시지를 읽어야 하므로 `throwOnError` 를 켠다.
 *
 * KaTeX 는 `trust: false`(기본값)에서 입력을 이스케이프한 뒤 자기 마크업만 만들어 내므로,
 * 여기서 나오는 HTML 은 사용자 입력이 아니라 KaTeX 의 출력이다.
 */
function MathPreview({ latex, displayMode }: MathPreviewProps) {
  const t = useTranslations('components.editor.math');

  const rendered = useMemo(() => {
    if (latex.length === 0) {
      return { html: null, error: null };
    }

    try {
      return {
        html: katex.renderToString(latex, { displayMode, throwOnError: true }),
        error: null,
      };
    } catch (cause) {
      return {
        html: null,
        error: cause instanceof Error ? cause.message : t('errors.invalid'),
      };
    }
  }, [latex, displayMode, t]);

  return (
    <div className="rounded-xl border bg-muted/30 px-3 py-4">
      {latex.length === 0 && (
        <p className="text-sm text-muted-foreground">{t('preview-empty')}</p>
      )}

      {rendered.html !== null && (
        <div
          className="overflow-x-auto"
          dangerouslySetInnerHTML={{ __html: rendered.html }}
        />
      )}

      {rendered.error !== null && (
        <p className="break-words text-xs text-destructive">{rendered.error}</p>
      )}
    </div>
  );
}
