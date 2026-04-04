import { Tabs } from 'antd';
import UserManagement from './UserManagement';
import AuditLogViewer from './AuditLogViewer';
import DataImportTab from './DataImportTab';
import AuditReportTab from './AuditReportTab';

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
  {
    key: 'import',
    label: '데이터 임포트',
    children: <DataImportTab />,
  },
  {
    key: 'report',
    label: '감사 보고서',
    children: <AuditReportTab />,
  },
];

export default function AdminPage() {
  return (
    <div>
      <Tabs defaultActiveKey="users" items={tabItems} />
    </div>
  );
}
