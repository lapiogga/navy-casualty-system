import { type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuth } from '../../hooks/useAuth';

interface GuardProps {
  children: ReactNode;
}

/** 미인증 사용자를 /login으로 리다이렉트 */
export function AuthGuard({ children }: GuardProps) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

/** 비밀번호 미변경 사용자를 /change-password로 리다이렉트 */
export function PasswordGuard({ children }: GuardProps) {
  const { user } = useAuth();

  if (user && !user.passwordChanged) {
    return <Navigate to="/change-password" replace />;
  }

  return <>{children}</>;
}

/** ADMIN 역할이 아닌 사용자를 /dead로 리다이렉트 */
export function AdminGuard({ children }: GuardProps) {
  const { isAdmin } = useAuth();

  if (!isAdmin) {
    return <Navigate to="/dead" replace />;
  }

  return <>{children}</>;
}
