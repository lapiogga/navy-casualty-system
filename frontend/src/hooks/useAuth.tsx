import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import type { AuthUser, Role } from '../types/auth';
import apiClient from '../api/client';

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAdmin: boolean;
  isManagerOrAbove: boolean;
  hasRole: (role: Role) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const ROLE_LEVEL: Record<Role, number> = {
  VIEWER: 0,
  OPERATOR: 1,
  MANAGER: 2,
  ADMIN: 3,
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const checkSession = useCallback(async () => {
    try {
      const res = await apiClient.get('/auth/me');
      setUser(res.data.data);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkSession();
  }, [checkSession]);

  const login = useCallback(async (username: string, password: string) => {
    const res = await apiClient.post('/auth/login', { username, password });
    setUser(res.data.data);
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout');
    } finally {
      setUser(null);
    }
  }, []);

  const hasRole = useCallback(
    (role: Role) => {
      if (!user) return false;
      return ROLE_LEVEL[user.role] >= ROLE_LEVEL[role];
    },
    [user],
  );

  const isAdmin = user?.role === 'ADMIN';
  const isManagerOrAbove = !!user && ROLE_LEVEL[user.role] >= ROLE_LEVEL.MANAGER;

  return (
    <AuthContext.Provider
      value={{ user, loading, login, logout, isAdmin, isManagerOrAbove, hasRole }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
