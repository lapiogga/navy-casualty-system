import { createBrowserRouter, Navigate } from 'react-router-dom';

// Task 2에서 실제 컴포넌트로 교체
const PlaceholderPage = ({ title }: { title: string }) => <div>{title}</div>;

const router = createBrowserRouter([
  {
    path: '/',
    children: [
      { index: true, element: <Navigate to="/dead" replace /> },
      { path: 'dead', element: <PlaceholderPage title="사망자 관리" /> },
      { path: 'wounded', element: <PlaceholderPage title="상이자 관리" /> },
      { path: 'review', element: <PlaceholderPage title="전공사상심사" /> },
      { path: 'document', element: <PlaceholderPage title="문서 출력" /> },
      { path: 'statistics', element: <PlaceholderPage title="통계/현황" /> },
      { path: 'admin', element: <PlaceholderPage title="시스템 관리" /> },
    ],
  },
  { path: '/login', element: <PlaceholderPage title="로그인" /> },
  { path: '*', element: <div>404</div> },
]);

export default router;
