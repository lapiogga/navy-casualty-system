export type DeadStatus = 'REGISTERED' | 'CONFIRMED' | 'NOTIFIED';

export interface DeadSearchParams {
  branchId?: number;
  serviceNumber?: string;
  name?: string;
  birthDate?: string;
  rankId?: number;
  unitId?: number;
  deathTypeId?: number;
  status?: DeadStatus;
  page?: number;
  size?: number;
}

export interface DeadResponse {
  id: number;
  serviceNumber: string;
  name: string;
  ssnMasked: string;
  birthDate: string;
  rankName: string;
  branchName: string;
  unitName: string;
  enlistmentDate: string;
  phone: string;
  deathTypeName: string;
  deathCodeSymbol: string;
  address: string;
  deathDate: string;
  status: string;
  createdAt: string;
}
