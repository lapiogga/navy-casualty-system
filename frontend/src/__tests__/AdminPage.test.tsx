import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import AdminPage from '../pages/admin/AdminPage';

// useAuth mock
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { username: 'admin', name: '관리자', role: 'ADMIN' },
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    isAdmin: true,
    isManagerOrAbove: true,
    hasRole: () => true,
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

function renderAdminPage() {
  return render(
    <MemoryRouter>
      <AdminPage />
    </MemoryRouter>,
  );
}

describe('AdminPage', () => {
  it('Tabs 컴포넌트가 렌더링된다', () => {
    renderAdminPage();
    expect(screen.getByRole('tablist')).toBeInTheDocument();
  });

  it('사용자 관리 탭이 존재한다', () => {
    renderAdminPage();
    expect(screen.getByRole('tab', { name: '사용자 관리' })).toBeInTheDocument();
  });

  it('감사 로그 탭이 존재한다', () => {
    renderAdminPage();
    expect(screen.getByRole('tab', { name: '감사 로그' })).toBeInTheDocument();
  });

  it('기본 선택 탭이 사용자 관리이다', () => {
    renderAdminPage();
    const usersTab = screen.getByRole('tab', { name: '사용자 관리' });
    expect(usersTab).toHaveAttribute('aria-selected', 'true');
  });
});
