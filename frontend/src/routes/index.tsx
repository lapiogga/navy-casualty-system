import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import DeadListPage from '../pages/dead/DeadListPage';
import WoundedListPage from '../pages/wounded/WoundedListPage';
import ReviewListPage from '../pages/review/ReviewListPage';
import DocumentIssueHistoryPage from '../pages/document/DocumentIssueHistoryPage';
import BranchStatPage from '../pages/statistics/BranchStatPage';
import MonthlyStatPage from '../pages/statistics/MonthlyStatPage';
import YearlyStatPage from '../pages/statistics/YearlyStatPage';
import UnitStatPage from '../pages/statistics/UnitStatPage';
import UnitRosterPage from '../pages/statistics/UnitRosterPage';
import AllRosterPage from '../pages/statistics/AllRosterPage';
import ErrorPage from '../pages/ErrorPage';
import NotFoundPage from '../pages/NotFoundPage';
import LoginPage from '../pages/LoginPage';
import ChangePasswordPage from '../pages/ChangePasswordPage';
import AdminPage from '../pages/admin/AdminPage';
import { AuthGuard, AdminGuard, PasswordGuard } from '../components/auth/AuthGuard';

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
