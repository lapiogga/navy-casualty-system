import { Tabs } from 'antd';
import UserManagement from './UserManagement';
import AuditLogViewer from './AuditLogViewer';

const tabItems = [
  {
    key: 'users',
    label: '사용자 관리',
    children: <UserManagement />,
  },
  {
    key: 'audit',
    label: '감사 로그',
    children: <AuditLogViewer />,
  },
];

export default function AdminPage() {
  return (
    <div>
      <Tabs defaultActiveKey="users" items={tabItems} />
    </div>
  );
}
