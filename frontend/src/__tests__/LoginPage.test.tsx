import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import LoginPage from '../pages/LoginPage';

// useAuth mock
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: null,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    isAdmin: false,
    isManagerOrAbove: false,
    hasRole: () => false,
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

function renderLoginPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  it('시스템 이름이 표시된다', () => {
    renderLoginPage();
    expect(screen.getByText('해군 사상자 관리 시스템')).toBeInTheDocument();
  });

  it('소속 정보가 표시된다', () => {
    renderLoginPage();
    expect(screen.getByText('해군본부 인사참모부')).toBeInTheDocument();
  });

  it('사용자 ID 입력 필드가 존재한다', () => {
    renderLoginPage();
    expect(screen.getByPlaceholderText('사용자 ID')).toBeInTheDocument();
  });

  it('비밀번호 입력 필드가 존재한다', () => {
    renderLoginPage();
    expect(screen.getByPlaceholderText('비밀번호')).toBeInTheDocument();
  });

  it('로그인 버튼이 존재한다', () => {
    renderLoginPage();
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument();
  });
});
