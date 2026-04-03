// 4종 통계 집계 응답 타입
export interface BranchStatResponse {
  branchName: string;
  count: number;
}

export interface MonthlyStatResponse {
  year: number;
  month: number;
  count: number;
}

export interface YearlyStatResponse {
  year: number;
  count: number;
}

export interface UnitStatResponse {
  unitName: string;
  count: number;
}

// 명부 응답 타입
export interface DeadRosterResponse {
  id: number;
  branchName: string;
  serviceNumber: string;
  name: string;
  ssnMasked: string;
  rankName: string;
  unitName: string;
  deathDate: string;
  deathTypeName: string;
  status: string;
}
