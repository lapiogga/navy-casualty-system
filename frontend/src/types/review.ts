// 공통 타입 재사용
export type { PageData, RankCode, BranchCode, UnitCode } from './dead';

export type ReviewClassification = 'COMBAT_WOUND' | 'DUTY_WOUND' | 'REJECTED' | 'DEFERRED';

export const CLASSIFICATION_LABELS: Record<ReviewClassification, string> = {
  COMBAT_WOUND: '전공상',
  DUTY_WOUND: '공상',
  REJECTED: '기각',
  DEFERRED: '보류',
};

export interface ReviewRecord {
  id: number;
  reviewRound: number;
  reviewDate: string | null;
  name: string;
  serviceNumber: string;
  ssnMasked: string;
  birthDate: string | null;
  rankName: string | null;
  branchName: string | null;
  unitName: string | null;
  enlistmentDate: string | null;
  diseaseName: string | null;
  unitReviewResult: string | null;
  classificationName: string | null;
  status: 'REGISTERED' | 'UNDER_REVIEW' | 'CONFIRMED' | 'NOTIFIED';
  notificationDate: string | null;
  createdAt: string;
}

export interface ReviewSearchParams {
  branchId?: number;
  serviceNumber?: string;
  name?: string;
  birthDate?: string;
  rankId?: number;
  unitId?: number;
  classification?: ReviewClassification;
  status?: string;
  page?: number;
  size?: number;
}

export interface ReviewCreateForm {
  reviewRound: number;
  reviewDate?: string;
  name: string;
  serviceNumber: string;
  ssn?: string;
  birthDate?: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  diseaseName?: string;
  unitReviewResult?: string;
  classification?: ReviewClassification;
}

export interface ReviewUpdateForm {
  reviewRound?: number;
  reviewDate?: string;
  name: string;
  ssn?: string;
  birthDate?: string;
  rankId?: number;
  branchId?: number;
  unitId?: number;
  enlistmentDate?: string;
  diseaseName?: string;
  unitReviewResult?: string;
  classification?: ReviewClassification;
}

// 이력 관련 타입 (REVW-06)
export interface ReviewSnapshot {
  reviewRound: number;
  reviewDate: string | null;
  name: string;
  serviceNumber: string;
  birthDate: string | null;
  enlistmentDate: string | null;
  unitId: number | null;
  rankId: number | null;
  branchId: number | null;
  diseaseName: string | null;
  unitReviewResult: string | null;
  classification: string | null;
  status: string | null;
  notificationDate: string | null;
}

export interface ReviewHistoryRecord {
  id: number;
  reviewId: number;
  reviewRound: number;
  snapshot: ReviewSnapshot;
  changedAt: string;
  changedBy: string;
}
