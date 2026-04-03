export interface DeadRecord {
  id: number;
  serviceNumber: string;
  name: string;
  ssnMasked: string;
  birthDate: string;
  rankName: string | null;
  branchName: string | null;
  unitName: string | null;
  enlistmentDate: string | null;
  phone: string | null;
  deathTypeName: string | null;
  deathCodeSymbol: string | null;
  address: string | null;
  deathDate: string;
  status: 'REGISTERED' | 'CONFIRMED' | 'NOTIFIED';
  createdAt: string;
}

export interface DeadSearchParams {
  branchId?: number;
  serviceNumber?: string;
  name?: string;
  birthDate?: string;
  rankId?: number;
  unitId?: number;
  deathTypeId?: number;
  status?: string;
  page?: number;
  size?: number;
}

export interface DeadCreateForm {
  serviceNumber: string;
  name: string;
  ssn: string;
  birthDate: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  phone?: string;
  deathTypeId?: number;
  deathCodeId?: number;
  address?: string;
  deathDate: string;
}

export interface DeadUpdateForm {
  name: string;
  ssn: string;
  birthDate: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  phone?: string;
  deathTypeId?: number;
  deathCodeId?: number;
  address?: string;
  deathDate: string;
}

export interface CodeItem {
  id: number;
  [key: string]: unknown;
}

export interface RankCode extends CodeItem {
  rankName: string;
  rankGroup: string;
  sortOrder: number;
}

export interface BranchCode extends CodeItem {
  branchName: string;
}

export interface DeathType extends CodeItem {
  typeName: string;
}

export interface DeathCode extends CodeItem {
  codeSymbol: string;
  codeName: string;
}

export interface UnitCode extends CodeItem {
  unitName: string;
  parentId: number | null;
}

export interface PageData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
