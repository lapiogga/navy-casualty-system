import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import PlaceholderPage from '../components/layout/PlaceholderPage';
import DeadListPage from '../pages/dead/DeadListPage';
import WoundedListPage from '../pages/wounded/WoundedListPage';
import ErrorPage from '../pages/ErrorPage';
import NotFoundPage from '../pages/NotFoundPage';
import LoginPage from '../pages/LoginPage';
import AdminPage from '../pages/admin/AdminPage';
import { AuthGuard, AdminGuard } from '../components/auth/AuthGuard';

const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <AuthGuard>
        <AppLayout />
      </AuthGuard>
    ),
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <Navigate to="/dead" replace /> },
      { path: 'dead', element: <DeadListPage /> },
      { path: 'wounded', element: <WoundedListPage /> },
      { path: 'review', element: <PlaceholderPage title="전공사상심사" /> },
      { path: 'document', element: <PlaceholderPage title="문서 출력" /> },
      { path: 'statistics', element: <PlaceholderPage title="통계/현황" /> },
      {
        path: 'admin',
        element: (
          <AdminGuard>
            <AdminPage />
          </AdminGuard>
        ),
      },
    ],
  },
  { path: '/login', element: <LoginPage /> },
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
