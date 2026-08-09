import { RobotIcon } from '@phosphor-icons/react/dist/ssr';
import { useTranslations } from 'next-intl';

/**
 * "{clientName} 으로 작성됨" 배지. 에이전트가 만든 글에만 붙는다.
 *
 * 출처는 `posts.created_via_client_id` 이고 한 번 정해지면 바뀌지 않으므로, 작성자가 초안을
 * 다듬어 발행한 뒤에도 이 표시는 남는다 — 누가 처음 썼는지를 감추지 않는다는 뜻이다.
 */
export function DraftedViaBadge({
  clientName,
}: {
  clientName?: string | null;
}) {
  const t = useTranslations('components.post.viewer');

  if (!clientName) {
    return null;
  }

  return (
    <span
      className="text-muted-foreground inline-flex items-center gap-1 text-xs"
      title={t('drafted-via', { clientName })}
    >
      <RobotIcon aria-hidden />
      {t('drafted-via', { clientName })}
    </span>
  );
}
