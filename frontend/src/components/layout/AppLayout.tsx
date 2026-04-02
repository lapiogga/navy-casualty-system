import { useState } from 'react';
import { Layout, Menu } from 'antd';
import {
  TeamOutlined,
  MedicineBoxOutlined,
  AuditOutlined,
  PrinterOutlined,
  BarChartOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Sider, Header, Content } = Layout;

const menuItems = [
  { key: '/dead', icon: <TeamOutlined />, label: '사망자 관리' },
  { key: '/wounded', icon: <MedicineBoxOutlined />, label: '상이자 관리' },
  { key: '/review', icon: <AuditOutlined />, label: '전공사상심사' },
  { key: '/document', icon: <PrinterOutlined />, label: '문서 출력' },
  { key: '/statistics', icon: <BarChartOutlined />, label: '통계/현황' },
  { key: '/admin', icon: <SettingOutlined />, label: '시스템 관리' },
];

export default function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        width={220}
        theme="dark"
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 600,
            fontSize: collapsed ? 14 : 16,
            whiteSpace: 'nowrap',
            overflow: 'hidden',
          }}
        >
          {collapsed ? '사상자' : '사상자 관리'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            height: 64,
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <span style={{ fontSize: 18, fontWeight: 600 }}>
            해군 사상자 관리 시스템
          </span>
          <span>{/* Phase 2: 사용자 정보 표시 영역 */}</span>
        </Header>
        <Content style={{ margin: 16, background: '#fff', padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
