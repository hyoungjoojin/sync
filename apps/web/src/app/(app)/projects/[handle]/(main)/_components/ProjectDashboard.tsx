'use client';

import { ChartBarIcon } from '@phosphor-icons/react';

import {
  Empty,
  EmptyContent,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from '@/components/ui/empty';

export default function ProjectDashboard() {
  return (
    <Empty className="pt-4">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <ChartBarIcon />
        </EmptyMedia>
        <EmptyTitle>대시보드 준비 중입니다</EmptyTitle>
        <EmptyDescription>
          프로젝트 대시보드는 곧 제공될 예정입니다.
        </EmptyDescription>
      </EmptyHeader>
      <EmptyContent />
    </Empty>
  );
}
