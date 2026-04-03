import { useState, useCallback } from 'react';
import { Card, Form, Input, Select, DatePicker, Button, Table, Tag, Space } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined, EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useAuth } from '../../hooks/useAuth';
import {
  useDeadList,
  useBranches,
  useRanks,
  useUnits,
  useDeathTypes,
  useUpdateDeadStatus,
  useExportDeadExcel,
} from '../../api/dead';
import type { DeadRecord, DeadSearchParams } from '../../types/dead';
import DeadFormModal from './DeadFormModal';
import DeadDeleteModal from './DeadDeleteModal';

const statusOptions = [
  { value: 'REGISTERED', label: '등록' },
  { value: 'CONFIRMED', label: '확인' },
  { value: 'NOTIFIED', label: '통보' },
];

const statusColorMap: Record<string, string> = {
  REGISTERED: 'blue',
  CONFIRMED: 'green',
  NOTIFIED: 'gold',
};

export default function DeadListPage() {
  const { hasRole } = useAuth();
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState<DeadSearchParams>({ page: 0, size: 10 });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchExpanded, setSearchExpanded] = useState(true);

  // Modal 상태
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<DeadRecord | null>(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteRecordId, setDeleteRecordId] = useState<number | null>(null);

  // 데이터 조회
  const { data, isLoading } = useDeadList({ ...searchParams, page: page - 1, size: pageSize });
  const { data: branches } = useBranches();
  const { data: ranks } = useRanks();
  const { data: units } = useUnits();
  const { data: deathTypes } = useDeathTypes();
  const updateStatus = useUpdateDeadStatus();
  const exportExcel = useExportDeadExcel();

  const handleSearch = useCallback(
    (values: Record<string, unknown>) => {
      const params: DeadSearchParams = {};
      if (values.branchId) params.branchId = values.branchId as number;
      if (values.serviceNumber) params.serviceNumber = values.serviceNumber as string;
      if (values.name) params.name = values.name as string;
      if (values.birthDate) params.birthDate = (values.birthDate as dayjs.Dayjs).format('YYYY-MM-DD');
      if (values.rankId) params.rankId = values.rankId as number;
      if (values.unitId) params.unitId = values.unitId as number;
      if (values.deathTypeId) params.deathTypeId = values.deathTypeId as number;
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

  const handleEdit = useCallback((record: DeadRecord) => {
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

  const columns: ColumnsType<DeadRecord> = [
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
    {
      title: '사망일자',
      dataIndex: 'deathDate',
      key: 'deathDate',
      width: 110,
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
    { title: '등록일', dataIndex: 'createdAt', key: 'createdAt', width: 110 },
    {
      title: '관리',
      key: 'actions',
      width: 200,
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
        title="사망자 검색"
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
            <Form.Item name="deathTypeId" label="사망구분">
              <Select
                allowClear
                placeholder="선택"
                style={{ width: 120 }}
                options={deathTypes?.map((t) => ({ value: t.id, label: t.typeName }))}
              />
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
      <Table<DeadRecord>
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
        scroll={{ x: 1200 }}
        size="small"
      />

      {/* 등록/수정 Modal */}
      <DeadFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false);
          setEditRecord(null);
        }}
        editRecord={editRecord}
      />

      {/* 삭제 Modal */}
      <DeadDeleteModal
        open={deleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setDeleteRecordId(null);
        }}
        recordId={deleteRecordId}
      />
    </div>
  );
}
