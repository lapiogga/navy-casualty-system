import { lazy } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import ErrorPage from '../pages/ErrorPage';
import NotFoundPage from '../pages/NotFoundPage';
import LoginPage from '../pages/LoginPage';
import { AuthGuard, AdminGuard, PasswordGuard } from '../components/auth/AuthGuard';

// lazy imports (페이지 컴포넌트만)
const DeadListPage = lazy(() => import('../pages/dead/DeadListPage'));
const WoundedListPage = lazy(() => import('../pages/wounded/WoundedListPage'));
const ReviewListPage = lazy(() => import('../pages/review/ReviewListPage'));
const DocumentIssueHistoryPage = lazy(() => import('../pages/document/DocumentIssueHistoryPage'));
const BranchStatPage = lazy(() => import('../pages/statistics/BranchStatPage'));
const MonthlyStatPage = lazy(() => import('../pages/statistics/MonthlyStatPage'));
const YearlyStatPage = lazy(() => import('../pages/statistics/YearlyStatPage'));
const UnitStatPage = lazy(() => import('../pages/statistics/UnitStatPage'));
const UnitRosterPage = lazy(() => import('../pages/statistics/UnitRosterPage'));
const AllRosterPage = lazy(() => import('../pages/statistics/AllRosterPage'));
const AdminPage = lazy(() => import('../pages/admin/AdminPage'));
const ChangePasswordPage = lazy(() => import('../pages/ChangePasswordPage'));

const router = createBrowserRouter([
  {
    path: '/change-password',
    element: (
      <AuthGuard>
        <ChangePasswordPage />
      </AuthGuard>
    ),
  },
  {
    path: '/',
    element: (
      <AuthGuard>
        <PasswordGuard>
          <AppLayout />
        </PasswordGuard>
      </AuthGuard>
    ),
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <Navigate to="/dead" replace /> },
      { path: 'dead', element: <DeadListPage /> },
      { path: 'wounded', element: <WoundedListPage /> },
      { path: 'review', element: <ReviewListPage /> },
      { path: 'document/history', element: <DocumentIssueHistoryPage /> },
      { path: 'statistics/branch', element: <BranchStatPage /> },
      { path: 'statistics/monthly', element: <MonthlyStatPage /> },
      { path: 'statistics/yearly', element: <YearlyStatPage /> },
      { path: 'statistics/unit', element: <UnitStatPage /> },
      { path: 'roster/unit', element: <UnitRosterPage /> },
      { path: 'roster/all', element: <AllRosterPage /> },
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
