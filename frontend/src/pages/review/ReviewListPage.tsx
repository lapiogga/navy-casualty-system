import { useState, useCallback } from 'react';
import { Card, Form, Input, Select, DatePicker, Button, Table, Tag, Space, Modal } from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DownOutlined,
  UpOutlined,
  EditOutlined,
  DeleteOutlined,
  HistoryOutlined,
  SendOutlined,
  DownloadOutlined,
  PrinterOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useAuth } from '../../hooks/useAuth';
import {
  useReviewList,
  useBranches,
  useRanks,
  useUnits,
  useUpdateReviewStatus,
  useRecordNotification,
  useExportReviewExcel,
} from '../../api/review';
import type { ReviewRecord, ReviewSearchParams, ReviewClassification } from '../../types/review';
import { CLASSIFICATION_LABELS } from '../../types/review';
import { DocumentType } from '../../types/document';
import ReviewFormModal from './ReviewFormModal';
import ReviewDeleteModal from './ReviewDeleteModal';
import ReviewHistoryDrawer from './ReviewHistoryDrawer';
import DocumentIssuePurposeModal from '../document/DocumentIssuePurposeModal';
import DocumentPreviewModal from '../document/DocumentPreviewModal';

const statusOptions = [
  { value: 'REGISTERED', label: '등록' },
  { value: 'UNDER_REVIEW', label: '심사중' },
  { value: 'CONFIRMED', label: '확인' },
  { value: 'NOTIFIED', label: '통보' },
];

const statusColorMap: Record<string, string> = {
  REGISTERED: 'blue',
  UNDER_REVIEW: 'orange',
  CONFIRMED: 'green',
  NOTIFIED: 'gold',
};

const classificationColorMap: Record<string, string> = {
  '전공상': 'red',
  '공상': 'orange',
  '기각': 'default',
  '보류': 'blue',
};

const classificationOptions = [
  { value: 'COMBAT_WOUND', label: '전공상' },
  { value: 'DUTY_WOUND', label: '공상' },
  { value: 'REJECTED', label: '기각' },
  { value: 'DEFERRED', label: '보류' },
];

export default function ReviewListPage() {
  const { hasRole } = useAuth();
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState<ReviewSearchParams>({ page: 0, size: 10 });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchExpanded, setSearchExpanded] = useState(true);

  // Modal / Drawer 상태
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<ReviewRecord | null>(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteRecordId, setDeleteRecordId] = useState<number | null>(null);
  const [historyDrawerReviewId, setHistoryDrawerReviewId] = useState<number | null>(null);

  // 문서 출력 Modal 상태
  const [docPurposeModalOpen, setDocPurposeModalOpen] = useState(false);
  const [selectedDocType, setSelectedDocType] = useState<DocumentType>(DocumentType.REVIEW_RESULT);
  const [selectedTargetId, setSelectedTargetId] = useState<number>(0);
  const [pdfBlob, setPdfBlob] = useState<Blob | null>(null);

  // 데이터 조회
  const { data, isLoading } = useReviewList({ ...searchParams, page: page - 1, size: pageSize });
  const { data: branches } = useBranches();
  const { data: ranks } = useRanks();
  const { data: units } = useUnits();
  const updateStatus = useUpdateReviewStatus();
  const recordNotification = useRecordNotification();
  const exportExcel = useExportReviewExcel();

  const handleSearch = useCallback(
    (values: Record<string, unknown>) => {
      const params: ReviewSearchParams = {};
      if (values.branchId) params.branchId = values.branchId as number;
      if (values.serviceNumber) params.serviceNumber = values.serviceNumber as string;
      if (values.name) params.name = values.name as string;
      if (values.birthDate) params.birthDate = (values.birthDate as dayjs.Dayjs).format('YYYY-MM-DD');
      if (values.rankId) params.rankId = values.rankId as number;
      if (values.unitId) params.unitId = values.unitId as number;
      if (values.classification) params.classification = values.classification as ReviewClassification;
      if (values.status) params.status = values.status as string;
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

  const handleEdit = useCallback((record: ReviewRecord) => {
    setEditRecord(record);
    setFormModalOpen(true);
  }, []);

  const handleDelete = useCallback((id: number) => {
    setDeleteRecordId(id);
    setDeleteModalOpen(true);
  }, []);

  const handleStatusChange = useCallback(
    (id: number, status: string) => {
      updateStatus.mutate({ id, status });
    },
    [updateStatus],
  );

  const handleDocPrint = useCallback((docType: DocumentType, targetId: number) => {
    setSelectedDocType(docType);
    setSelectedTargetId(targetId);
    setDocPurposeModalOpen(true);
  }, []);

  const handleDocSuccess = useCallback((blob: Blob) => {
    setDocPurposeModalOpen(false);
    setPdfBlob(blob);
  }, []);

  const handleNotify = useCallback(
    (record: ReviewRecord) => {
      Modal.confirm({
        title: '보훈청 통보',
        content: `${record.name}님의 심사 결과를 보훈청에 통보하시겠습니까?`,
        okText: '통보',
        cancelText: '취소',
        onOk: () => recordNotification.mutate(record.id),
      });
    },
    [recordNotification],
  );

  const columns: ColumnsType<ReviewRecord> = [
    {
      title: '번호',
      key: 'index',
      width: 60,
      render: (_, __, index) => (page - 1) * pageSize + index + 1,
    },
    { title: '심사차수', dataIndex: 'reviewRound', key: 'reviewRound', width: 80 },
    { title: '군구분', dataIndex: 'branchName', key: 'branchName', width: 80 },
    { title: '군번', dataIndex: 'serviceNumber', key: 'serviceNumber', width: 120 },
    { title: '성명', dataIndex: 'name', key: 'name', width: 80 },
    { title: '주민번호', dataIndex: 'ssnMasked', key: 'ssnMasked', width: 140 },
    { title: '계급', dataIndex: 'rankName', key: 'rankName', width: 80 },
    { title: '소속', dataIndex: 'unitName', key: 'unitName', width: 120 },
    { title: '심사일자', dataIndex: 'reviewDate', key: 'reviewDate', width: 110 },
    { title: '병명', dataIndex: 'diseaseName', key: 'diseaseName', width: 120 },
    { title: '소속부대 심사결과', dataIndex: 'unitReviewResult', key: 'unitReviewResult', width: 140 },
    {
      title: '분류',
      dataIndex: 'classificationName',
      key: 'classificationName',
      width: 80,
      render: (value: string | null) =>
        value ? (
          <Tag color={classificationColorMap[value] || 'default'}>{value}</Tag>
        ) : (
          '-'
        ),
    },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={statusColorMap[status] || 'default'}>
          {statusOptions.find((o) => o.value === status)?.label || status}
        </Tag>
      ),
    },
    { title: '보훈청 통보일', dataIndex: 'notificationDate', key: 'notificationDate', width: 120 },
    { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', width: 110 },
    {
      title: '문서출력',
      key: 'document',
      width: 120,
      render: (_, record) => (
        <Button
          size="small"
          icon={<PrinterOutlined />}
          onClick={() => handleDocPrint(DocumentType.REVIEW_RESULT, record.id)}
        >
          심사결과서
        </Button>
      ),
    },
    {
      title: '관리',
      key: 'actions',
      width: 300,
      render: (_, record) => (
        <Space size="small">
          {hasRole('OPERATOR') && (
            <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
              수정
            </Button>
          )}
          {hasRole('MANAGER') && (
            <>
              <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
                삭제
              </Button>
              {record.status === 'REGISTERED' && (
                <Button size="small" onClick={() => handleStatusChange(record.id, 'UNDER_REVIEW')}>
                  심사
                </Button>
              )}
              {record.status === 'UNDER_REVIEW' && (
                <Button size="small" onClick={() => handleStatusChange(record.id, 'CONFIRMED')}>
                  확인
                </Button>
              )}
              {record.status === 'CONFIRMED' && (
                <Button
                  size="small"
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={() => handleNotify(record)}
                >
                  보훈청 통보
                </Button>
              )}
            </>
          )}
          <Button
            size="small"
            icon={<HistoryOutlined />}
            onClick={() => setHistoryDrawerReviewId(record.id)}
          >
            이력
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* 검색 폼 */}
      <Card
        title="전공사상심사 검색"
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
            <Form.Item name="branchId" label="군구분">
              <Select
                allowClear
                placeholder="선택"
                style={{ width: 120 }}
                options={branches?.map((b) => ({ value: b.id, label: b.branchName }))}
              />
            </Form.Item>
            <Form.Item name="serviceNumber" label="군번">
              <Input placeholder="군번" style={{ width: 120 }} />
            </Form.Item>
            <Form.Item name="name" label="성명">
              <Input placeholder="성명" style={{ width: 100 }} />
            </Form.Item>
            <Form.Item name="birthDate" label="생년월일">
              <DatePicker placeholder="생년월일" />
            </Form.Item>
            <Form.Item name="rankId" label="계급">
              <Select
                allowClear
                placeholder="선택"
                style={{ width: 120 }}
                options={ranks?.map((r) => ({ value: r.id, label: r.rankName }))}
              />
            </Form.Item>
            <Form.Item name="unitId" label="소속">
              <Select
                allowClear
                placeholder="선택"
                style={{ width: 140 }}
                options={units?.map((u) => ({ value: u.id, label: u.unitName }))}
              />
            </Form.Item>
            <Form.Item name="classification" label="분류">
              <Select allowClear placeholder="선택" style={{ width: 100 }} options={classificationOptions} />
            </Form.Item>
            <Form.Item name="status" label="상태">
              <Select allowClear placeholder="선택" style={{ width: 100 }} options={statusOptions} />
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
        {hasRole('OPERATOR') && (
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditRecord(null);
              setFormModalOpen(true);
            }}
          >
            등록
          </Button>
        )}
        <Button
          icon={<DownloadOutlined />}
          onClick={() => exportExcel.mutate(searchParams)}
          loading={exportExcel.isPending}
        >
          Excel 다운로드
        </Button>
      </div>

      {/* 테이블 */}
      <Table<ReviewRecord>
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
        scroll={{ x: 1800 }}
        size="small"
      />

      {/* 등록/수정 Modal */}
      <ReviewFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false);
          setEditRecord(null);
        }}
        editRecord={editRecord}
      />

      {/* 삭제 Modal */}
      <ReviewDeleteModal
        open={deleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setDeleteRecordId(null);
        }}
        recordId={deleteRecordId}
      />

      {/* 이력 Drawer */}
      <ReviewHistoryDrawer
        reviewId={historyDrawerReviewId}
        open={historyDrawerReviewId !== null}
        onClose={() => setHistoryDrawerReviewId(null)}
      />

      {/* 문서 발급 목적 Modal */}
      <DocumentIssuePurposeModal
        open={docPurposeModalOpen}
        documentType={selectedDocType}
        targetId={selectedTargetId}
        onSuccess={handleDocSuccess}
        onCancel={() => setDocPurposeModalOpen(false)}
      />

      {/* PDF 미리보기 Modal */}
      <DocumentPreviewModal
        pdfBlob={pdfBlob}
        documentType={selectedDocType}
        onClose={() => setPdfBlob(null)}
      />
    </div>
  );
}
