import type { PostProjectSummary } from '../types';

/**
 * 제목 위에 붙는 한 줄. 읽는 시간은 커버 위 배지(`ReadingTimeBadge`)로 옮겨서
 * 여기에는 어느 스페이스의 글인지만 남는다.
 */
export function ArticleMetaLine({ project }: { project?: PostProjectSummary }) {
  if (!project?.name) {
    return null;
  }

  return <div className="text-muted-foreground text-xs">{project.name}</div>;
}
