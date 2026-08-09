import CollectionView from './_components/CollectionView';

export default async function CollectionPage({
  params,
}: {
  params: Promise<{ externalId: string }>;
}) {
  const { externalId } = await params;

  return <CollectionView externalId={externalId} />;
}
