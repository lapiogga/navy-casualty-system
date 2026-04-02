import type { Role } from '../types/auth';

/**
 * 주민등록번호를 역할에 따라 마스킹한다.
 * - VIEWER: 전체 마스킹 (******-*******)
 * - OPERATOR: 뒤 6자리 마스킹 (앞 8자리 노출)
 * - MANAGER, ADMIN: 전체 노출
 */
export function maskRrn(rrn: string | null | undefined, role: Role): string {
  if (!rrn) return '';
  switch (role) {
    case 'VIEWER':
      return '******-*******';
    case 'OPERATOR':
      return rrn.substring(0, 8) + '******';
    default:
      return rrn; // MANAGER, ADMIN
  }
}
