import { useEffect, useRef, useState } from 'react';
import { Modal, Button, Space } from 'antd';
import { PrinterOutlined, DownloadOutlined } from '@ant-design/icons';
import { DocumentType, DOCUMENT_TYPE_LABELS } from '../../types/document';

interface Props {
  pdfBlob: Blob | null;
  documentType: DocumentType;
  onClose: () => void;
}

/**
 * PDF 미리보기 Modal (DOCU-09).
 * iframe으로 PDF를 표시하고, 인쇄/다운로드 버튼을 제공한다.
 */
export default function DocumentPreviewModal({ pdfBlob, documentType, onClose }: Props) {
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);

  useEffect(() => {
    if (pdfBlob) {
      const url = URL.createObjectURL(pdfBlob);
      setBlobUrl(url);
      return () => {
        URL.revokeObjectURL(url);
      };
    }
    setBlobUrl(null);
  }, [pdfBlob]);

  const handlePrint = () => {
    iframeRef.current?.contentWindow?.print();
  };

  const handleDownload = () => {
    if (!blobUrl) return;
    const a = document.createElement('a');
    a.href = blobUrl;
    a.download = `${DOCUMENT_TYPE_LABELS[documentType]}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  return (
    <Modal
      title={`미리보기 - ${DOCUMENT_TYPE_LABELS[documentType]}`}
      open={pdfBlob !== null}
      onCancel={onClose}
      width="80vw"
      footer={
        <Space>
          <Button icon={<PrinterOutlined />} onClick={handlePrint}>
            인쇄
          </Button>
          <Button type="primary" icon={<DownloadOutlined />} onClick={handleDownload}>
            다운로드
          </Button>
        </Space>
      }
      destroyOnClose
    >
      {blobUrl && (
        <iframe
          ref={iframeRef}
          src={blobUrl}
          style={{ width: '100%', height: '80vh', border: 'none' }}
          title="PDF 미리보기"
        />
      )}
    </Modal>
  );
}
