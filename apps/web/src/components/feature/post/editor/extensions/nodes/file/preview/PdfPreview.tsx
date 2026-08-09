'use client';

import { CaretLeftIcon, CaretRightIcon } from '@phosphor-icons/react';
import { useTranslations } from 'next-intl';
import { useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';

// 워커는 번들러가 처리한 자산 URL로 가리킨다. CDN 을 쓰면 프로덕션의 CSP 가 막고,
// 버전이 react-pdf 가 기대하는 pdfjs 와 어긋날 수 있다.
pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString();

const PREVIEW_WIDTH = 720;

export function PdfPreview({ blob }: { blob: Blob }) {
  const t = useTranslations('components.editor.file.preview');

  const [pageCount, setPageCount] = useState(0);
  const [pageNumber, setPageNumber] = useState(1);

  return (
    <div className="flex flex-col items-center gap-3">
      {/* Blob 을 그대로 넘긴다. objectURL 을 만들면 개발 모드의 이중 마운트에서
          정리 단계가 URL 을 먼저 회수해 pdf.js 가 빈 응답을 받는다. */}
      <Document
        file={blob}
        onLoadSuccess={({ numPages }) => {
          setPageCount(numPages);
          setPageNumber(1);
        }}
        loading={<Spinner className="size-5" />}
        error={
          <p className="py-6 text-sm text-destructive">{t('unavailable')}</p>
        }
        className="max-w-full overflow-x-auto"
      >
        <Page
          pageNumber={pageNumber}
          width={PREVIEW_WIDTH}
          loading={<Spinner className="size-5" />}
        />
      </Document>

      {pageCount > 1 && (
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            aria-label={t('previous-page')}
            disabled={pageNumber <= 1}
            onClick={() => setPageNumber((current) => Math.max(1, current - 1))}
          >
            <CaretLeftIcon className="size-4" />
          </Button>

          <span className="text-xs text-muted-foreground">
            {t('page-of', { page: pageNumber, total: pageCount })}
          </span>

          <Button
            variant="outline"
            size="icon"
            aria-label={t('next-page')}
            disabled={pageNumber >= pageCount}
            onClick={() =>
              setPageNumber((current) => Math.min(pageCount, current + 1))
            }
          >
            <CaretRightIcon className="size-4" />
          </Button>
        </div>
      )}
    </div>
  );
}
