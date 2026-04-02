import { useState, useCallback } from 'react';
import { Table, Form, Input, Select, Button, DatePicker, Space } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import apiClient from '../../api/client';

const { RangePicker } = DatePicker;

interface AuditLog {
  id: number;
  createdAt: string;
  userId: string;
  action: string;
  targetTable: string;
  targetId: string;
  detail: string;
  ipAddress: string;
}

interface PageResponse {
  content: AuditLog[];
  totalElements: number;
}

interface SearchFilter {
  dateRange?: [Dayjs, Dayjs];
  userId?: string;
  action?: string;
}

const actionOptions = [
  { value: '', label: '전체' },
  { value: 'VIEW', label: 'VIEW' },
  { value: 'CREATE', label: 'CREATE' },
  { value: 'UPDATE', label: 'UPDATE' },
  { value: 'DELETE', label: 'DELETE' },
  { value: 'PRINT', label: 'PRINT' },
  { value: 'EXPORT', label: 'EXPORT' },
];

const columns: ColumnsType<AuditLog> = [
  { title: '일시', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '사용자ID', dataIndex: 'userId', key: 'userId', width: 120 },
  { title: '작업유형', dataIndex: 'action', key: 'action', width: 100 },
  { title: '대상테이블', dataIndex: 'targetTable', key: 'targetTable', width: 140 },
  { title: '대상ID', dataIndex: 'targetId', key: 'targetId', width: 100 },
  { title: '상세내용', dataIndex: 'detail', key: 'detail', ellipsis: true },
  { title: 'IP주소', dataIndex: 'ipAddress', key: 'ipAddress', width: 140 },
];

export default function AuditLogViewer() {
  const [data, setData] = useState<AuditLog[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize] = useState(20);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<SearchFilter>({});

  const fetchLogs = useCallback(
    async (page: number, size: number, searchFilter: SearchFilter) => {
      setLoading(true);
      try {
        const params: Record<string, string | number> = {
          page: page - 1,
          size,
        };
        if (searchFilter.dateRange) {
          params.startDate = searchFilter.dateRange[0].format('YYYY-MM-DD');
          params.endDate = searchFilter.dateRange[1].format('YYYY-MM-DD');
        }
        if (searchFilter.userId) {
          params.userId = searchFilter.userId;
        }
        if (searchFilter.action) {
          params.action = searchFilter.action;
        }

        const res = await apiClient.get<PageResponse>('/admin/audit-logs', { params });
        setData(res.data.content);
        setTotal(res.data.totalElements);
      } catch {
        // 에러 시 빈 데이터 유지
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  const handleSearch = (values: SearchFilter) => {
    setFilter(values);
    setCurrent(1);
    fetchLogs(1, pageSize, values);
  };

  const handlePageChange = (page: number) => {
    setCurrent(page);
    fetchLogs(page, pageSize, filter);
  };

  return (
    <div>
      <Form<SearchFilter>
        layout="inline"
        onFinish={handleSearch}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="dateRange" label="기간">
          <RangePicker />
        </Form.Item>
        <Form.Item name="userId" label="사용자ID">
          <Input placeholder="사용자 ID" allowClear />
        </Form.Item>
        <Form.Item name="action" label="작업유형">
          <Select
            options={actionOptions}
            placeholder="전체"
            allowClear
            style={{ width: 120 }}
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
            조회
          </Button>
        </Form.Item>
      </Form>

      <Table<AuditLog>
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{
          current,
          pageSize,
          total,
          onChange: handlePageChange,
        }}
        scroll={{ x: 900 }}
      />
    </div>
  );
}
