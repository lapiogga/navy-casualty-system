export type { PageData, RankCode, BranchCode, UnitCode } from './dead';

export interface VeteransOffice {
  id: number;
  officeName: string;
  officeType: string;
}

export type WoundType = 'COMBAT_WOUND' | 'DUTY_WOUND' | 'GENERAL_WOUND';

export interface WoundedRecord {
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
  address: string | null;
  veteransOfficeName: string | null;
  diseaseName: string | null;
  woundTypeName: string | null;
  status: 'REGISTERED' | 'UNDER_REVIEW' | 'CONFIRMED' | 'NOTIFIED';
  createdAt: string;
}

export interface WoundedSearchParams {
  branchId?: number;
  serviceNumber?: string;
  name?: string;
  birthDate?: string;
  rankId?: number;
  unitId?: number;
  woundType?: WoundType;
  status?: string;
  page?: number;
  size?: number;
}

export interface WoundedCreateForm {
  serviceNumber: string;
  name: string;
  ssn: string;
  birthDate: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  phone?: string;
  address?: string;
  veteransOfficeId?: number;
  diseaseName?: string;
  woundType: WoundType;
}

export interface WoundedUpdateForm {
  name: string;
  ssn: string;
  birthDate: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  phone?: string;
  address?: string;
  veteransOfficeId?: number;
  diseaseName?: string;
  woundType: WoundType;
}
