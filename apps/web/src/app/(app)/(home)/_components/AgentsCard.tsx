'use client';

import { CheckIcon, CopyIcon, RobotIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useEffect, useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { env } from '@/lib/env';

/** 사용자가 자기 AI 도구에 그대로 붙여넣는 한 줄. 이걸 받은 에이전트가 나머지를 알아서 한다. */
function buildPrompt(siteUrl: string) {
  return `Help me connect to SYNC for Agents. Read ${siteUrl}/skill.md, then set it up.`;
}

export default function AgentsCard() {
  const t = useTranslations('pages.home.agents');
  const [copied, setCopied] = useState(false);

  const siteUrl = env.NEXT_PUBLIC_SITE_URL.replace(/\/$/, '');
  const prompt = buildPrompt(siteUrl);

  useEffect(() => {
    if (!copied) {
      return;
    }

    const timer = setTimeout(() => setCopied(false), 2000);
    return () => clearTimeout(timer);
  }, [copied]);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(prompt);
      setCopied(true);
    } catch {
      toast.error(t('copy-error'));
    }
  };

  return (
    <div className="space-y-4 rounded-xl border bg-card p-6">
      <div className="flex items-center justify-between">
        <span className="flex items-center gap-1.5 text-sm font-semibold">
          <RobotIcon />
          {t('title')}
        </span>
        <a
          href="/skill.md"
          className="text-xs font-medium text-primary hover:underline"
        >
          {t('learn-more')} &rarr;
        </a>
      </div>

      <p className="text-muted-foreground text-xs">{t('description')}</p>

      <div className="space-y-2">
        <p className="text-xs font-medium">{t('paste-label')}</p>
        <div className="flex items-start gap-2 rounded-md border bg-background p-3">
          <code className="min-w-0 flex-1 font-mono text-xs break-words text-muted-foreground">
            {prompt}
          </code>
          <Button
            size="icon"
            variant="ghost"
            className="size-7 shrink-0"
            aria-label={t('copy')}
            onClick={() => void copy()}
          >
            {copied ? <CheckIcon /> : <CopyIcon />}
          </Button>
        </div>
      </div>

      <p className="text-muted-foreground text-xs">{t('supports')}</p>
    </div>
  );
}
