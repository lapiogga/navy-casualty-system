import { useState, useCallback } from 'react';
import { Card, Form, Input, Select, DatePicker, Button, Table, Space } from 'antd';
import { SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined, FileTextOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useDocumentIssues } from '../../api/document';
import { DocumentType, DOCUMENT_TYPE_LABELS } from '../../types/document';
import type { DocumentIssueResponse, DocumentIssueSearchParams } from '../../types/document';
import DocumentIssuePurposeModal from './DocumentIssuePurposeModal';
import DocumentPreviewModal from './DocumentPreviewModal';

const documentTypeOptions = Object.values(DocumentType).map((value) => ({
  value,
  label: DOCUMENT_TYPE_LABELS[value],
}));

/**
 * 문서 발급 이력 조회 페이지.
 * 검색 폼 + Ant Design Table + 서버사이드 페이징.
 */
export default function DocumentIssueHistoryPage() {
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState<DocumentIssueSearchParams>({ page: 0, size: 10 });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchExpanded, setSearchExpanded] = useState(true);

  // 문서 출력 Modal 상태 (발급대장 출력용)
  const [docPurposeModalOpen, setDocPurposeModalOpen] = useState(false);
  const [pdfBlob, setPdfBlob] = useState<Blob | null>(null);

  const { data, isLoading } = useDocumentIssues({ ...searchParams, page: page - 1, size: pageSize });

  const handleSearch = useCallback(
    (values: Record<string, unknown>) => {
      const params: DocumentIssueSearchParams = {};
      if (values.documentType) params.documentType = values.documentType as DocumentType;
      if (values.issuedBy) params.issuedBy = values.issuedBy as string;
      if (values.dateRange) {
        const range = values.dateRange as [dayjs.Dayjs, dayjs.Dayjs];
        params.startDate = range[0].format('YYYY-MM-DD');
        params.endDate = range[1].format('YYYY-MM-DD');
      }
      setSearchParams(params);
      setPage(1);
    },
    [],
  );

  const handleReset = useCallback(() => {
    form.resetFields();
    setSearchParams({});
    setPage(1);
  }, [form]);

  const handleDocSuccess = useCallback((blob: Blob) => {
    setDocPurposeModalOpen(false);
    setPdfBlob(blob);
  }, []);

  const columns: ColumnsType<DocumentIssueResponse> = [
    {
      title: '번호',
      key: 'index',
      width: 60,
      render: (_, __, index) => (page - 1) * pageSize + index + 1,
    },
    {
      title: '문서유형',
      dataIndex: 'documentTypeName',
      key: 'documentTypeName',
      width: 200,
    },
    { title: '대상테이블', dataIndex: 'targetTable', key: 'targetTable', width: 140 },
    { title: '대상ID', dataIndex: 'targetId', key: 'targetId', width: 80 },
    { title: '발급목적', dataIndex: 'issuePurpose', key: 'issuePurpose', width: 250 },
    { title: '발급자', dataIndex: 'issuedBy', key: 'issuedBy', width: 100 },
    {
      title: '발급일시',
      dataIndex: 'issuedAt',
      key: 'issuedAt',
      width: 180,
      render: (value: string) => value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
  ];

  return (
    <div>
      {/* 검색 폼 */}
      <Card
        title="문서 발급 이력 검색"
        size="small"
        style={{ marginBottom: 16 }}
        extra={
          <Button
            type="link"
            size="small"
            icon={searchExpanded ? <UpOutlined /> : <DownOutlined />}
            onClick={() => setSearchExpanded((prev) => !prev)}
          >
            {searchExpanded ? '접기' : '펼치기'}
          </Button>
        }
      >
        {searchExpanded && (
          <Form form={form} layout="inline" onFinish={handleSearch} style={{ flexWrap: 'wrap', gap: 8 }}>
            <Form.Item name="documentType" label="문서유형">
              <Select
                allowClear
                placeholder="선택"
                style={{ width: 220 }}
                options={documentTypeOptions}
              />
            </Form.Item>
            <Form.Item name="issuedBy" label="발급자">
              <Input placeholder="발급자" style={{ width: 120 }} />
            </Form.Item>
            <Form.Item name="dateRange" label="발급일">
              <DatePicker.RangePicker />
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                  조회
                </Button>
                <Button onClick={handleReset} icon={<ReloadOutlined />}>
                  초기화
                </Button>
              </Space>
            </Form.Item>
          </Form>
        )}
      </Card>

      {/* 상단 버튼 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
        <Button
          icon={<FileTextOutlined />}
          onClick={() => setDocPurposeModalOpen(true)}
        >
          발급대장 출력
        </Button>
      </div>

      {/* 테이블 */}
      <Table<DocumentIssueResponse>
        rowKey="id"
        columns={columns}
        dataSource={data?.content}
        loading={isLoading}
        pagination={{
          current: page,
          pageSize,
          total: data?.totalElements,
          showSizeChanger: true,
          showTotal: (total) => `총 ${total}건`,
          onChange: (p, size) => {
            setPage(p);
            setPageSize(size);
          },
        }}
        scroll={{ x: 1000 }}
        size="small"
      />

      {/* 발급대장 출력 - 발급 목적 Modal */}
      <DocumentIssuePurposeModal
        open={docPurposeModalOpen}
        documentType={DocumentType.ISSUE_LEDGER}
        targetId={0}
        onSuccess={handleDocSuccess}
        onCancel={() => setDocPurposeModalOpen(false)}
      />

      {/* PDF 미리보기 Modal */}
      <DocumentPreviewModal
        pdfBlob={pdfBlob}
        documentType={DocumentType.ISSUE_LEDGER}
        onClose={() => setPdfBlob(null)}
      />
    </div>
  );
}
