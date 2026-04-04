import { Suspense, useMemo, useState } from 'react';
import { Layout, Menu, Button, Space, Spin } from 'antd';
import {
  TeamOutlined,
  MedicineBoxOutlined,
  AuditOutlined,
  FileTextOutlined,
  BarChartOutlined,
  SettingOutlined,
  LogoutOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const { Sider, Header, Content } = Layout;

const allMenuItems = [
  { key: '/dead', icon: <TeamOutlined />, label: '사망자 관리' },
  { key: '/wounded', icon: <MedicineBoxOutlined />, label: '상이자 관리' },
  { key: '/review', icon: <AuditOutlined />, label: '전공사상심사' },
  { key: '/document/history', icon: <FileTextOutlined />, label: '발급 이력' },
  {
    key: 'status-sub',
    icon: <BarChartOutlined />,
    label: '현황',
    children: [
      { key: '/statistics/branch', label: '신분별 사망자 현황' },
      { key: '/statistics/monthly', label: '월별 사망자 현황' },
      { key: '/statistics/yearly', label: '연도별 사망자 현황' },
      { key: '/statistics/unit', label: '부대별 사망자 현황' },
    ],
  },
  {
    key: 'roster-sub',
    icon: <UnorderedListOutlined />,
    label: '명부',
    children: [
      { key: '/roster/unit', label: '부대별 사망자 명부' },
      { key: '/roster/all', label: '전사망자 명부' },
    ],
  },
  { key: '/admin', icon: <SettingOutlined />, label: '시스템 관리', adminOnly: true },
];

export default function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAdmin } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  // 현황/명부 SubMenu 자동 open
  const defaultOpenKeys = useMemo(() => {
    if (location.pathname.startsWith('/statistics')) return ['status-sub'];
    if (location.pathname.startsWith('/roster')) return ['roster-sub'];
    return [];
  }, [location.pathname]);

  // AUTH-06: ADMIN이 아닌 사용자에게 시스템 관리 메뉴 숨김
  const menuItems = useMemo(
    () =>
      allMenuItems
        .filter((item) => !item.adminOnly || isAdmin)
        .map(({ adminOnly: _, ...rest }) => rest),
    [isAdmin],
  );

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

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
          defaultOpenKeys={defaultOpenKeys}
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
          <Space>
            <span>
              {user?.name} ({user?.role})
            </span>
            <Button
              icon={<LogoutOutlined />}
              onClick={handleLogout}
              type="text"
            >
              로그아웃
            </Button>
          </Space>
        </Header>
        <Content style={{ margin: 16, background: '#fff', padding: 24 }}>
          <Suspense
            fallback={
              <div style={{ display: 'flex', justifyContent: 'center', padding: 100 }}>
                <Spin size="large" />
              </div>
            }
          >
            <Outlet />
          </Suspense>
        </Content>
      </Layout>
    </Layout>
  );
}
