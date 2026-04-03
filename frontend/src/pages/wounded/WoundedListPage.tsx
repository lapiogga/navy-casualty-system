import { useState, useCallback } from 'react';
import { Card, Form, Input, Select, DatePicker, Button, Table, Tag, Space, Dropdown } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined, EditOutlined, DeleteOutlined, DownloadOutlined, PrinterOutlined, FileTextOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useAuth } from '../../hooks/useAuth';
import {
  useWoundedList,
  useBranches,
  useRanks,
  useUnits,
  useUpdateWoundedStatus,
  useVeteransOffices,
  useExportWoundedExcel,
} from '../../api/wounded';
import type { WoundedRecord, WoundedSearchParams } from '../../types/wounded';
import { DocumentType } from '../../types/document';
import WoundedFormModal from './WoundedFormModal';
import WoundedDeleteModal from './WoundedDeleteModal';
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

const woundTypeOptions = [
  { value: 'COMBAT_WOUND', label: '전공상' },
  { value: 'DUTY_WOUND', label: '공상' },
  { value: 'GENERAL_WOUND', label: '일반상이' },
];

export default function WoundedListPage() {
  const { hasRole } = useAuth();
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState<WoundedSearchParams>({ page: 0, size: 10 });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchExpanded, setSearchExpanded] = useState(true);

  // Modal 상태
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<WoundedRecord | null>(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteRecordId, setDeleteRecordId] = useState<number | null>(null);

  // 문서 출력 Modal 상태
  const [docPurposeModalOpen, setDocPurposeModalOpen] = useState(false);
  const [selectedDocType, setSelectedDocType] = useState<DocumentType>(DocumentType.WOUNDED_CERTIFICATE);
  const [selectedTargetId, setSelectedTargetId] = useState<number>(0);
  const [pdfBlob, setPdfBlob] = useState<Blob | null>(null);

  // 데이터 조회
  const { data, isLoading } = useWoundedList({ ...searchParams, page: page - 1, size: pageSize });
  const { data: branches } = useBranches();
  const { data: ranks } = useRanks();
  const { data: units } = useUnits();
  const { data: veteransOffices } = useVeteransOffices();
  const updateStatus = useUpdateWoundedStatus();
  const exportExcel = useExportWoundedExcel();

  const handleSearch = useCallback(
    (values: Record<string, unknown>) => {
      const params: WoundedSearchParams = {};
      if (values.branchId) params.branchId = values.branchId as number;
      if (values.serviceNumber) params.serviceNumber = values.serviceNumber as string;
      if (values.name) params.name = values.name as string;
      if (values.birthDate) params.birthDate = (values.birthDate as dayjs.Dayjs).format('YYYY-MM-DD');
      if (values.rankId) params.rankId = values.rankId as number;
      if (values.unitId) params.unitId = values.unitId as number;
      if (values.woundType) params.woundType = values.woundType as WoundedSearchParams['woundType'];
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

  const handleEdit = useCallback((record: WoundedRecord) => {
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

  const columns: ColumnsType<WoundedRecord> = [
    {
      title: '번호',
      key: 'index',
      width: 60,
      render: (_, __, index) => (page - 1) * pageSize + index + 1,
    },
    { title: '군구분', dataIndex: 'branchName', key: 'branchName', width: 80 },
    { title: '군번', dataIndex: 'serviceNumber', key: 'serviceNumber', width: 120 },
    { title: '성명', dataIndex: 'name', key: 'name', width: 80 },
    { title: '주민번호', dataIndex: 'ssnMasked', key: 'ssnMasked', width: 140 },
    { title: '계급', dataIndex: 'rankName', key: 'rankName', width: 80 },
    { title: '소속', dataIndex: 'unitName', key: 'unitName', width: 120 },
    { title: '보훈청명', dataIndex: 'veteransOfficeName', key: 'veteransOfficeName', width: 120 },
    { title: '병명', dataIndex: 'diseaseName', key: 'diseaseName', width: 120 },
    { title: '상이구분', dataIndex: 'woundTypeName', key: 'woundTypeName', width: 100 },
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
    { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', width: 110 },
    {
      title: '문서출력',
      key: 'document',
      width: 120,
      render: (_, record) => (
        <Button
          size="small"
          icon={<PrinterOutlined />}
          onClick={() => handleDocPrint(DocumentType.WOUNDED_CERTIFICATE, record.id)}
        >
          확인서 출력
        </Button>
      ),
    },
    {
      title: '관리',
      key: 'actions',
      width: 240,
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
                <Button size="small" onClick={() => handleStatusChange(record.id, 'NOTIFIED')}>
                  통보
                </Button>
              )}
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* 검색 폼 */}
      <Card
        title="상이자 검색"
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
            <Form.Item name="woundType" label="상이구분">
              <Select allowClear placeholder="선택" style={{ width: 120 }} options={woundTypeOptions} />
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
        <Button
          icon={<FileTextOutlined />}
          onClick={() => handleDocPrint(DocumentType.WOUNDED_STATUS_REPORT, 0)}
        >
          상이자 현황 보고서
        </Button>
      </div>

      {/* 테이블 */}
      <Table<WoundedRecord>
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
        scroll={{ x: 1400 }}
        size="small"
      />

      {/* 등록/수정 Modal */}
      <WoundedFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false);
          setEditRecord(null);
        }}
        editRecord={editRecord}
      />

      {/* 삭제 Modal */}
      <WoundedDeleteModal
        open={deleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setDeleteRecordId(null);
        }}
        recordId={deleteRecordId}
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
