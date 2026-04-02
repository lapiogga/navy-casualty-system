import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import AppLayout from '../components/layout/AppLayout';

function renderWithRouter() {
  return render(
    <MemoryRouter initialEntries={['/dead']}>
      <AppLayout />
    </MemoryRouter>
  );
}

describe('AppLayout', () => {
  it('시스템 타이틀을 렌더링한다', () => {
    renderWithRouter();
    expect(screen.getByText('해군 사상자 관리 시스템')).toBeInTheDocument();
  });

  it('6개 메뉴 항목을 렌더링한다', () => {
    renderWithRouter();
    const menuLabels = [
      '사망자 관리',
      '상이자 관리',
      '전공사상심사',
      '문서 출력',
      '통계/현황',
      '시스템 관리',
    ];
    for (const label of menuLabels) {
      expect(screen.getByText(label)).toBeInTheDocument();
    }
  });

  it('Sider가 width 220으로 렌더링된다', () => {
    const { container } = renderWithRouter();
    const sider = container.querySelector('.ant-layout-sider');
    expect(sider).toBeInTheDocument();
  });
});
