import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import PlaceholderPage from '../components/layout/PlaceholderPage';
import ErrorPage from '../pages/ErrorPage';
import NotFoundPage from '../pages/NotFoundPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    errorElement: <ErrorPage />,
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
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
