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
  woundTypeLabel: string | null;
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
  woundType?: string;
  status?: string;
  page?: number;
  size?: number;
}

export interface PageData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
