import { GeneratedPostCover } from './GeneratedPostCover';

/** 크기는 감싸는 쪽에서 정한다. 여기서는 자리를 가득 채우기만 한다. */
export function ArticlePreviewMedia({
  seed,
  coverImageUrl,
}: {
  seed: string;
  coverImageUrl?: string | null;
}) {
  if (!coverImageUrl) {
    return <GeneratedPostCover seed={seed} className="h-full w-full" />;
  }

  return (
    <div
      className="h-full w-full bg-cover bg-center"
      style={{ backgroundImage: `url(${coverImageUrl})` }}
    />
  );
}
