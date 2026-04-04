export type Role = 'ADMIN' | 'MANAGER' | 'OPERATOR' | 'VIEWER';

export interface AuthUser {
  username: string;
  name: string;
  role: Role;
  passwordChanged: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}
