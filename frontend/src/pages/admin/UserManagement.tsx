import { useState, useCallback, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Popconfirm, Space, message, Tag } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Role } from '../../types/auth';

interface UserRecord {
  id: number;
  username: string;
  name: string;
  role: Role;
  enabled: boolean;
  accountLocked: boolean;
  lastLoginAt: string | null;
}

interface PageResponse {
  content: UserRecord[];
  totalElements: number;
}

interface CreateUserForm {
  username: string;
  name: string;
  password: string;
  role: Role;
}

const roleOptions: { value: Role; label: string }[] = [
  { value: 'ADMIN', label: 'ADMIN' },
  { value: 'MANAGER', label: 'MANAGER' },
  { value: 'OPERATOR', label: 'OPERATOR' },
  { value: 'VIEWER', label: 'VIEWER' },
];

export default function UserManagement() {
  const [data, setData] = useState<UserRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<CreateUserForm>();

  const fetchUsers = useCallback(async (page: number, size: number) => {
    setLoading(true);
    try {
      const res = await apiClient.get<PageResponse>('/admin/users', {
        params: { page: page - 1, size },
      });
      setData(res.data.content);
      setTotal(res.data.totalElements);
    } catch {
      message.error('사용자 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers(current, pageSize);
  }, [current, pageSize, fetchUsers]);

  const refresh = () => fetchUsers(current, pageSize);

  const handleCreate = async (values: CreateUserForm) => {
    try {
      await apiClient.post('/admin/users', values);
      message.success('사용자가 생성되었습니다');
      setModalOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.error('사용자 생성에 실패했습니다');
    }
  };

  const handleRoleChange = async (id: number, role: Role) => {
    try {
      await apiClient.put(`/admin/users/${id}/role`, { role });
      message.success('역할이 변경되었습니다');
      refresh();
    } catch {
      message.error('역할 변경에 실패했습니다');
    }
  };

  const handleUnlock = async (id: number) => {
    try {
      await apiClient.post(`/admin/users/${id}/unlock`);
      message.success('잠금이 해제되었습니다');
      refresh();
    } catch {
      message.error('잠금 해제에 실패했습니다');
    }
  };

  const handleForceLogout = async (id: number) => {
    try {
      await apiClient.post(`/admin/users/${id}/force-logout`);
      message.success('강제 로그아웃 되었습니다');
      refresh();
    } catch {
      message.error('강제 로그아웃에 실패했습니다');
    }
  };

  const columns: ColumnsType<UserRecord> = [
    { title: '사용자ID', dataIndex: 'username', key: 'username' },
    { title: '이름', dataIndex: 'name', key: 'name' },
    {
      title: '역할',
      dataIndex: 'role',
      key: 'role',
      render: (role: Role, record) => (
        <Select
          value={role}
          size="small"
          style={{ width: 120 }}
          options={roleOptions}
          onChange={(value) => handleRoleChange(record.id, value)}
        />
      ),
    },
    {
      title: '상태',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? '활성' : '비활성'}
        </Tag>
      ),
    },
    {
      title: '잠금',
      dataIndex: 'accountLocked',
      key: 'accountLocked',
      render: (locked: boolean) => (
        <Tag color={locked ? 'red' : 'default'}>
          {locked ? '잠금' : '정상'}
        </Tag>
      ),
    },
    { title: '최종로그인', dataIndex: 'lastLoginAt', key: 'lastLoginAt' },
    {
      title: '관리',
      key: 'actions',
      render: (_, record) => (
        <Space size="small">
          <Popconfirm
            title="잠금을 해제하시겠습니까?"
            onConfirm={() => handleUnlock(record.id)}
          >
            <Button size="small" disabled={!record.accountLocked}>
              잠금 해제
            </Button>
          </Popconfirm>
          <Popconfirm
            title="강제 로그아웃 하시겠습니까?"
            onConfirm={() => handleForceLogout(record.id)}
          >
            <Button size="small">강제 로그아웃</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setModalOpen(true)}
        >
          사용자 추가
        </Button>
      </div>

      <Table<UserRecord>
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{
          current,
          pageSize,
          total,
          onChange: (page) => setCurrent(page),
        }}
      />

      <Modal
        title="사용자 추가"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="등록"
        cancelText="취소"
      >
        <Form<CreateUserForm>
          form={form}
          layout="vertical"
          onFinish={handleCreate}
        >
          <Form.Item
            name="username"
            label="사용자 ID"
            rules={[{ required: true, message: '사용자 ID를 입력하세요' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="name"
            label="이름"
            rules={[{ required: true, message: '이름을 입력하세요' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="password"
            label="비밀번호"
            rules={[{ required: true, message: '비밀번호를 입력하세요' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="role"
            label="역할"
            rules={[{ required: true, message: '역할을 선택하세요' }]}
          >
            <Select options={roleOptions} placeholder="역할 선택" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
